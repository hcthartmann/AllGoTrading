package org.yats.connectivity.xchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Mapping;
import org.yats.common.Tool;
import org.yats.trading.*;

import java.io.IOException;

/**
 * Created
 * Date: 12/04/15
 * Time: 13:10
 */
public class TradingConnection implements Runnable, IConsumeOrders
{

    public void start() {
        thread.start();
    }
    public boolean isRunning() {
        return running;
    }
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public void sendOrderNew(OrderNew newOrder) {
        if(!mapTradablePid2XPid.containsKey(newOrder.getProductId())) return;
        Receipt receipt = newOrder.createReceiptDefault();
        mapOid2Receipt.put(receipt.getOrderIdString(), receipt);
        try {
            tradingProvider.sendOrderNew(newOrder);
        }catch(RuntimeException r) {
            r.printStackTrace();
            receipt.setEndState(true);
            receipt.setRejectReason(r.getMessage());
        }
        receiptConsumer.onReceipt(receipt);
    }

    @Override
    public void sendOrderCancel(OrderCancel o) {
        if(!mapTradablePid2XPid.containsKey(o.getProductId())) return;
        tradingProvider.sendOrderCancel(o);
    }

    @Override
    public void run() {
        while(!shutdown)
        {
            try {
                Thread.yield();
                tradingProvider.updateReceipts();
                sendChangedReceipts();
                sleepABitIfNotShuttingDown(5000);
            }
            catch(RuntimeException r) {
                log.error(r.getMessage());
                Tool.sleepFor(1000);
            }
        }
        running=false;
    }

    private void sendChangedReceipts()
    {
        for(Receipt receipt : mapOid2Receipt.values()) {
            if(receipt.isEndState()) continue;
            Receipt updatedReceipt = tradingProvider.getReceipt(receipt.getOrderId());
            if(receipt.isSameAs(updatedReceipt)) continue;
            mapOid2Receipt.put(updatedReceipt.getOrderIdString(), updatedReceipt);
            receiptConsumer.onReceipt(updatedReceipt);
        }
    }

    public TradingConnection(Mapping<String, String> _mapTradablePid2XPid, IProvideTrading _tradingProvider, IConsumeReceipt _receiptConsumer)
    {
        mapTradablePid2XPid = _mapTradablePid2XPid;
        tradingProvider = _tradingProvider;
        receiptConsumer = _receiptConsumer;
        mapOid2Receipt = new Mapping<String, Receipt>();
        thread = new Thread(this);
        running=false;
        shutdown=false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void sleepABitIfNotShuttingDown(int _sleepTimeMillis) {
        int timeSliceMillis = 200;
        int sleepSlices = _sleepTimeMillis / timeSliceMillis;
        for(int i=0; i<sleepSlices; i++) {
            if(shutdown) return;
            Tool.sleepFor(timeSliceMillis);
        }
    }

    private boolean running;
    private boolean shutdown;
    private Mapping<String,String> mapTradablePid2XPid;
    private IProvideTrading tradingProvider;
    private Thread thread;
    private Mapping<String, Receipt> mapOid2Receipt;
    private IConsumeReceipt receiptConsumer;


    final private Logger log = LoggerFactory.getLogger(TradingConnection.class);

}
