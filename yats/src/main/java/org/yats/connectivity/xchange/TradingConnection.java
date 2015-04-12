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

    }

    @Override
    public void sendOrderCancel(OrderCancel o) {

    }

    @Override
    public void run() {
        while(!shutdown)
        {
            try {
                Thread.yield();
                sleepABitIfNotShuttingDown(5000);
            }
            catch(RuntimeException r) {
                log.error(r.getMessage());
                Tool.sleepFor(1000);
            }
        }
        running=false;

    }

    public TradingConnection(Mapping<String, String> mapTradablePid2XPid, IProvideTrading tradingProvider)
    {
        this.mapTradablePid2XPid = mapTradablePid2XPid;
        this.tradingProvider = tradingProvider;
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
    final private Logger log = LoggerFactory.getLogger(TradingConnection.class);

}
