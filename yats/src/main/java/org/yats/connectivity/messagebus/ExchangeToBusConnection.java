package org.yats.connectivity.messagebus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IAmCalledBack;
import org.yats.common.PropertiesReader;
import org.yats.common.UniqueId;
import org.yats.connectivity.xchange.Pricefeeder;
import org.yats.connectivity.xchange.TradingConnection;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.*;

/**
 * Created
 * Date: 12/04/15
 * Time: 13:37
 */
public class ExchangeToBusConnection implements IAmCalledBack
{

    public void start() {
        receiverOrderNew.setObserver(this);
        receiverOrderNew.start();
        receiverOrderCancel.setObserver(this);
        receiverOrderCancel.start();
    }

    public void shutdown()
    {
        shuttingDown=true;
        receiverOrderNew.close();
        receiverOrderCancel.close();
    }

    @Override
    public void onCallback()
    {

    }

    public static class Factory {
        public ExchangeToBusConnection create(PropertiesReader _prop, TradingConnection _tradingConnection)
        {
            Config config =  Config.fromProperties(_prop);
            Sender<ReceiptMsg> senderReceipt = new Sender<ReceiptMsg>(config.getExchangeReceipts(), config.getServerIP());
            BufferingReceiver<OrderNewMsg> receiverOrderNew = new BufferingReceiver<OrderNewMsg>(OrderNewMsg.class,
                    config.getExchangeOrderNew(),
                    "#",
                    config.getServerIP());
            BufferingReceiver<OrderCancelMsg> receiverOrderCancel = new BufferingReceiver<OrderCancelMsg>(OrderCancelMsg.class,
                    config.getExchangeOrderCancel(),
                    "#",
                    config.getServerIP());
            ExchangeToBusConnection p = new ExchangeToBusConnection(
                    _tradingConnection,
                    senderReceipt,
                    receiverOrderNew,
                    receiverOrderCancel);
            return p;
        }
    }

    public ExchangeToBusConnection(
            TradingConnection _tradingConnection,
            Sender<ReceiptMsg> _senderReceipt,
            BufferingReceiver<OrderNewMsg> _receiverOrderNew,
            BufferingReceiver<OrderCancelMsg> _receiverOrderCancel)
    {
        tradingConnection = _tradingConnection;
        senderReceipt = _senderReceipt;
        receiverOrderNew = _receiverOrderNew;
        receiverOrderCancel = _receiverOrderCancel;
        shuttingDown=false;
    }


    /////////////////////////////////////////////////////////////////////////

    private boolean shuttingDown;
    TradingConnection tradingConnection;
    Sender<ReceiptMsg> senderReceipt;
    BufferingReceiver<OrderNewMsg> receiverOrderNew;
    BufferingReceiver<OrderCancelMsg> receiverOrderCancel;

    private final Logger log = LoggerFactory.getLogger(ExchangeToBusConnection.class);

}
