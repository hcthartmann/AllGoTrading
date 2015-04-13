package org.yats.connectivity.messagebus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IAmCalledBack;
import org.yats.common.PropertiesReader;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.OrderCancelMsg;
import org.yats.messagebus.messages.OrderNewMsg;
import org.yats.messagebus.messages.ReceiptMsg;
import org.yats.trading.IConsumeOrders;
import org.yats.trading.IConsumeReceipt;
import org.yats.trading.Receipt;

/**
 * Created
 * Date: 2015-04-13
 * Time: 16:42
 */
public class BusToExchangeConnection implements IAmCalledBack
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
    public synchronized void onCallback()
    {
        while(receiverOrderCancel.hasMoreMessages()) {
            if(shuttingDown) return;
            tradingConnection.sendOrderCancel(receiverOrderCancel.take().toOrderCancel());
        }
        while(receiverOrderNew.hasMoreMessages()) {
            if(shuttingDown) return;
            tradingConnection.sendOrderNew(receiverOrderNew.take().toOrderNew());
        }
    }

    public static class Factory {
        public BusToExchangeConnection create(PropertiesReader _prop, IConsumeOrders _tradingConnection)
        {
            Config config =  Config.fromProperties(_prop);
            BufferingReceiver<OrderNewMsg> receiverOrderNew = new BufferingReceiver<OrderNewMsg>(OrderNewMsg.class,
                    config.getExchangeOrderNew(),
                    "#",
                    config.getServerIP());
            BufferingReceiver<OrderCancelMsg> receiverOrderCancel = new BufferingReceiver<OrderCancelMsg>(OrderCancelMsg.class,
                    config.getExchangeOrderCancel(),
                    "#",
                    config.getServerIP());
            BusToExchangeConnection p = new BusToExchangeConnection(
                    _tradingConnection,
                    receiverOrderNew,
                    receiverOrderCancel);
            return p;
        }
    }

    public BusToExchangeConnection(
            IConsumeOrders _tradingConnection,
            BufferingReceiver<OrderNewMsg> _receiverOrderNew,
            BufferingReceiver<OrderCancelMsg> _receiverOrderCancel)
    {
        tradingConnection = _tradingConnection;
        receiverOrderNew = _receiverOrderNew;
        receiverOrderCancel = _receiverOrderCancel;
        shuttingDown=false;
    }


    /////////////////////////////////////////////////////////////////////////

    private boolean shuttingDown;
    IConsumeOrders tradingConnection;
    BufferingReceiver<OrderNewMsg> receiverOrderNew;
    BufferingReceiver<OrderCancelMsg> receiverOrderCancel;

    private final Logger log = LoggerFactory.getLogger(BusToExchangeConnection.class);

}
