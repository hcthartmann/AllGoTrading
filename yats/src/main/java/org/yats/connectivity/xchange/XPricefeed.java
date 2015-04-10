package org.yats.connectivity.xchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.*;
import org.yats.trading.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
                Tool.sleepFor(5000);
            }
            catch(IOException e) {
                log.error("network error");
                Tool.sleepFor(1000);
            }
        }
        running=false;
    }

    public XPricefeed(List<String> _subscribableProductIds, IProvidePriceData _priceDataProvider) {
        subscribableProductIds = _subscribableProductIds;
        priceDataProvider=_priceDataProvider;
        subscriptionList = new ConcurrentHashMap<String, IConsumePriceData>();
        thread = new Thread(this);
        shutdown = false;
        running=true;
    }

    //////////////////////////////////////////////////////////////////////////////////////

    private void receive() throws IOException {

        for(Map.Entry<String,IConsumePriceData> e : subscriptionList.entrySet())
        {
            String pid = e.getKey();
            IConsumePriceData priceDataConsumer = e.getValue();
            PriceData data = priceDataProvider.getPriceData(pid);
            priceDataConsumer.onPriceData(data);
        }
    }

    private boolean running;
    private boolean shutdown;
    private Thread thread;
    private ConcurrentHashMap<String, IConsumePriceData> subscriptionList;
    private IProvidePriceData priceDataProvider;
    private List<String> subscribableProductIds;

    final private Logger log = LoggerFactory.getLogger(XPricefeed.class);

}
