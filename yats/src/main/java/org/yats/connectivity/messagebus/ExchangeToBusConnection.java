package org.yats.connectivity.messagebus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IAmCalledBack;
import org.yats.common.PropertiesReader;
import org.yats.connectivity.xchange.TradingConnection;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.*;
import org.yats.trading.IConsumeOrders;
import org.yats.trading.IConsumeReceipt;
import org.yats.trading.Receipt;

/**
 * Created
 * Date: 12/04/15
 * Time: 13:37
 */

public class ExchangeToBusConnection implements IConsumeReceipt
{

    @Override
    public void onReceipt(Receipt receipt) {
        ReceiptMsg m = ReceiptMsg.fromReceipt(receipt);
        log.info("Published: " + receipt);
        senderReceipt.publish(m.getTopic(), m);
    }

    public static class Factory {
        public ExchangeToBusConnection create(PropertiesReader _prop)
        {
            Config config =  Config.fromProperties(_prop);
            Sender<ReceiptMsg> senderReceipt = new Sender<ReceiptMsg>(config.getExchangeReceipts(), config.getServerIP());
            ExchangeToBusConnection p = new ExchangeToBusConnection(senderReceipt);
            return p;
        }
    }

    public ExchangeToBusConnection(
            Sender<ReceiptMsg> _senderReceipt
    )
    {
        senderReceipt = _senderReceipt;
    }


    /////////////////////////////////////////////////////////////////////////

    Sender<ReceiptMsg> senderReceipt;

    private final Logger log = LoggerFactory.getLogger(ExchangeToBusConnection.class);

}
