package org.yats.connectivity.messagebus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IAmCalledBack;
import org.yats.common.PropertiesReader;
import org.yats.common.UniqueId;
import org.yats.connectivity.xchange.Pricefeeder;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.*;
import org.yats.trading.IConsumePriceData;
import org.yats.trading.PriceData;

/**
 * Created
 * Date: 09/04/15
 * Time: 20:07
 */
public class PricefeedToBusConnection implements IConsumePriceData, IAmCalledBack
{
    public void start() {
        receiverSubscription.setObserver(this);
        receiverSubscription.start();
    }

    public void shutdown()
    {
        shuttingDown=true;
        receiverSubscription.close();
    }

    @Override
    public synchronized void onCallback() {
        subscribeAllReceivedSubscriptions();
    }


    @Override
    public void onPriceData(PriceData priceData)
    {
        if(shuttingDown) return;
        PriceDataMsg data = PriceDataMsg.createFrom(priceData);
        log.info("Published: "+ priceData);
        senderPriceDataMsg.publish(data.getTopic(), data);
    }

    @Override
    public UniqueId getConsumerId() {
        return consumerId;
    }


    public static class Factory {
        public PricefeedToBusConnection create(PropertiesReader _prop, Pricefeeder _priceFeed) {

            Config config =  Config.fromProperties(_prop);
            Sender<PriceDataMsg> senderPriceDataMsg
                    = new Sender<PriceDataMsg>(config.getExchangePriceData(), config.getServerIP());
            BufferingReceiver<SubscriptionMsg> receiverSubscription
                    = new BufferingReceiver<SubscriptionMsg>(SubscriptionMsg.class,
                    config.getExchangeSubscription(),
                    config.getTopicSubscriptions(),
                    config.getServerIP());

            PricefeedToBusConnection p = new PricefeedToBusConnection(
                    UniqueId.create(), _priceFeed,
                    senderPriceDataMsg, receiverSubscription
            );
            return p;
        }
    }

    public PricefeedToBusConnection(UniqueId _consumerId, Pricefeeder _pricefeed,
                                    Sender<PriceDataMsg> _senderPriceDataMsg,
                                    BufferingReceiver<SubscriptionMsg> _receiverSubscription )
    {
        consumerId = _consumerId;
        priceFeed=_pricefeed;
        shuttingDown=false;
        senderPriceDataMsg = _senderPriceDataMsg;
        receiverSubscription=_receiverSubscription;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////

    private void subscribeAllReceivedSubscriptions()  {
        while(receiverSubscription.hasMoreMessages()) {
            if(shuttingDown) return;
            SubscriptionMsg m = receiverSubscription.get();
            log.info("Subscription received and forwarded for "+m.productId);
            priceFeed.subscribe(m.productId, this);
        }
    }

    private UniqueId consumerId;
    private Pricefeeder priceFeed;
    private boolean shuttingDown;
    private Sender<PriceDataMsg> senderPriceDataMsg;
    private BufferingReceiver<SubscriptionMsg> receiverSubscription;

    private final Logger log = LoggerFactory.getLogger(PricefeedToBusConnection.class);
}
