package org.yats.connectivity.xchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.*;
import org.yats.trading.*;

import java.io.IOException;
import java.util.List;


/**
 * Created
 * Date: 02/04/15
 * Time: 00:34
 */

public class XPricefeed implements IProvidePriceFeed, Runnable {


    public void start() {
        thread.start();
    }

    @Override
    public void subscribe(String productId, IConsumePriceData consumer) {
        if(subscriptionList.containsKey(productId)) {
            log.debug("product already subscribed: "+productId);
            return;
        }
        if(!subscribableProductIds.contains(productId)) {
            log.debug("product not available for subscription: "+productId);
            return;
        }
        log.debug("new subscription: "+productId);
        subscriptionList.put(productId,consumer);
    }

    public boolean isRunning() {
        return running;
    }

    public void shutdown() {
        shutdown = true;
    }

    @Override
    public void run() {
        while(!shutdown)
        {
            try {
                Thread.yield();
                receive();
                sleepABitIfNotShuttingDown(5000);
            }
            catch(IOException e) {
                log.error("network error");
                Tool.sleepFor(1000);
            }
        }
        running=false;
    }

    private void sleepABitIfNotShuttingDown(int _sleepTimeMillis) {
        int timeSliceMillis = 200;
        int sleepSlices = _sleepTimeMillis / timeSliceMillis;
        for(int i=0; i<sleepSlices; i++) {
            if(shutdown) return;
            Tool.sleepFor(timeSliceMillis);
        }
    }

    public XPricefeed(List<String> _subscribableProductIds, IProvidePriceData _priceDataProvider) {
        subscribableProductIds = _subscribableProductIds;
        priceDataProvider=_priceDataProvider;
        subscriptionList = new Mapping<String, IConsumePriceData>();
        cache = new Mapping<String, PriceData>();
        thread = new Thread(this);
        shutdown = false;
        running=true;
    }

    //////////////////////////////////////////////////////////////////////////////////////

    private void receive() throws IOException
    {
        for(String pid : subscriptionList.keyList())
        {
            PriceData newData = priceDataProvider.getPriceData(pid);
            sendIfChanged(newData);
        }
    }

    private void sendIfChanged(PriceData newData)
    {
        String pid = newData.getProductId();
        if(cache.containsKey(pid))   {
            PriceData oldData = cache.get(pid);
            if(oldData.isSameBestRowsAs(newData, 5)) {
                return;
            }
        }

        cache.put(pid, newData);
        IConsumePriceData priceDataConsumer = subscriptionList.get(pid);
        priceDataConsumer.onPriceData(newData);

    }

    private boolean running;
    private boolean shutdown;
    private Thread thread;
    private Mapping<String, IConsumePriceData> subscriptionList;
    private IProvidePriceData priceDataProvider;
    private List<String> subscribableProductIds;
    private Mapping<String, PriceData> cache;
    final private Logger log = LoggerFactory.getLogger(XPricefeed.class);

}
