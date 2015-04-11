package org.yats.connectivity.general;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.*;
import org.yats.connectivity.messagebus.StrategyToBusConnection;
import org.yats.messagebus.*;
import org.yats.messagebus.messages.PriceDataMsg;
import org.yats.messagebus.messages.SubscriptionMsg;
import org.yats.trading.IConsumePriceData;
import org.yats.trading.PriceData;

import java.util.Enumeration;


public class LastPriceServer implements IConsumePriceData, IAmCalledBack {



    public void start() {
        readCacheFromDisk();
        strategyToBusConnection.setPriceDataConsumer(this);
        receiverSubscription.setObserver(this);
        receiverSubscription.start();
    }

    public void close() {
        writeCacheToDisk();
        strategyToBusConnection.close();
        receiverSubscription.close();
    }

    @Override
    public void onPriceData(PriceData _priceData)
    {
        if(isNewerThanInCache(_priceData))
            cache.put(_priceData.getProductId(), _priceData);
    }

    public PriceData getLatest(String _pid) {
        return cache.get(_pid);
    }

    @Override
    public UniqueId getConsumerId() {
        return UniqueId.create();
    }

    @Override
    public synchronized void onCallback() {
        while(receiverSubscription.hasMoreMessages()) {
            String productId = receiverSubscription.get().productId;
            if(cache.containsKey(productId)) {
                PriceDataMsg d = PriceDataMsg.createFrom(cache.get(productId));
                senderPriceDataMsg.publish(d.getTopic(), d);
            }
        }
    }

    public LastPriceServer(IProvideProperties _prop)
    {
        shutdown=false;
        cacheFilename = _prop.get("cacheFilename");
        cache = new Map<String, PriceData>();
        readCacheFromDisk();
        Config config =  Config.fromProperties(_prop);
        strategyToBusConnection = new StrategyToBusConnection(_prop);
        strategyToBusConnection.setPriceDataConsumer(this);

        receiverSubscription = new BufferingReceiver<SubscriptionMsg>(SubscriptionMsg.class,
                config.getExchangeSubscription(),
                config.getTopicSubscriptions(),
                config.getServerIP());
        receiverSubscription.setObserver(this);
        receiverSubscription.start();
        senderPriceDataMsg = new Sender<PriceDataMsg>(config.getExchangePriceData(), config.getServerIP());
    }

    public static class Factory {
        public LastPriceServer createFromProperties(IProvideProperties _prop) {
            Config config =  Config.fromProperties(_prop);
            StrategyToBusConnection strategyToBusConnection = new StrategyToBusConnection(_prop);
            BufferingReceiver<SubscriptionMsg> receiverSubscription
                    = new BufferingReceiver<SubscriptionMsg>(SubscriptionMsg.class,
                    config.getExchangeSubscription(),
                    config.getTopicSubscriptions(),
                    config.getServerIP());
            Sender<PriceDataMsg> senderPriceDataMsg = new Sender<PriceDataMsg>(config.getExchangePriceData(), config.getServerIP());

            return new LastPriceServer(_prop.get("cacheFilename"), strategyToBusConnection,
                    receiverSubscription,senderPriceDataMsg);
        }
    }


    public LastPriceServer(String _cacheFilename, StrategyToBusConnection _connection,
                           BufferingReceiver<SubscriptionMsg> _subscriptionReceiver,
                           Sender<PriceDataMsg> _senderPriceDataMsg)
    {
        shutdown=false;
        cacheFilename = _cacheFilename;
        cache = new Map<String, PriceData>();
        strategyToBusConnection = _connection;
        receiverSubscription = _subscriptionReceiver;
        senderPriceDataMsg = _senderPriceDataMsg;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private void writeCacheToDisk() {
        StringBuilder b = new StringBuilder();
        Serializer<PriceDataMsg> serializer = new Serializer<PriceDataMsg>();

        Enumeration<String> cacheKeys = cache.keys();
        while(cacheKeys.hasMoreElements()) {
            String key = cacheKeys.nextElement();
            PriceData d = cache.get(key);
            PriceDataMsg m = PriceDataMsg.createFrom(d);
            String s = serializer.convertToString(m);
            b.append(s).append("\n");
        }
        FileTool.writeToTextFile(cacheFilename, b.toString(), false);
        log.info("Cache written to disk with "+cache.size()+" items.");
    }

    private void readCacheFromDisk() {
        if(!FileTool.exists(cacheFilename)) return;
        cache.clear();
        StringBuilder b = new StringBuilder();
        Deserializer<PriceDataMsg> deserializer = new Deserializer<PriceDataMsg>(PriceDataMsg.class);
        String wholeFile = FileTool.readFromTextFile(cacheFilename);
        String[] parts = wholeFile.split("\n");
        for(String s : parts) {
            if(s.length()==0) continue;
            PriceDataMsg m = deserializer.convertFromString(s);
            PriceData d = m.toPriceData();
            cache.put(d.getProductId(), d);
        }
        log.info("Cache read from disk with "+cache.size()+" items.");
    }

    private boolean isNewerThanInCache(PriceData priceData)
    {
        String productId = priceData.getProductId();
        if(!cache.containsKey(productId)) return true;
        PriceData oldData = cache.get(productId);
        return priceData.isAfter(oldData);
    }


    private final String cacheFilename;
    private Sender<PriceDataMsg> senderPriceDataMsg;
    private Map<String, PriceData> cache;
    private BufferingReceiver<SubscriptionMsg> receiverSubscription;
    private final boolean shutdown;
    private final StrategyToBusConnection strategyToBusConnection;

    private final Logger log = LoggerFactory.getLogger(LastPriceServer.class);

//    private final IProvideProperties prop;
}
