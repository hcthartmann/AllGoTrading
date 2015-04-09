package org.yats.connectivity.xchange;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.bitfinex.v1.BitfinexExchange;
import com.xeiam.xchange.bitfinex.v1.dto.marketdata.BitfinexDepth;
import com.xeiam.xchange.bitfinex.v1.dto.marketdata.BitfinexLevel;
import com.xeiam.xchange.bitfinex.v1.service.polling.BitfinexMarketDataServiceRaw;
import com.xeiam.xchange.service.polling.marketdata.PollingMarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.common.Tool;
import org.yats.trading.IConsumePriceData;
import org.yats.trading.IProvidePriceFeed;
import org.yats.trading.IProvideProduct;
import org.yats.trading.PriceData;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created
 * Date: 02/04/15
 * Time: 00:34
 */

public class XPriceFeed implements IProvidePriceFeed, Runnable {

    final Logger log = LoggerFactory.getLogger(XPriceFeed.class);


    @Override
    public void subscribe(String productId, IConsumePriceData consumer) {
        priceDataConsumer = consumer;
        if(subscriptionList.containsKey(productId)) return;
        if(!properties.exists(productId)) {
            log.debug("Subscription not available:"+productId);
            return;
        }
        log.debug("New subscription:"+productId);
        subscriptionList.put(productId,consumer);
        String symbol = properties.get(productId);
        mapPidToSymbol.put(productId, symbol);
        mapSymbolToPid.put(symbol, productId);
        stopReceiving=true;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        while(!shutdown)
        {
            try {
                Thread.yield();
                receive();
                Tool.sleepFor(500);
            }
            catch(IOException e) {
                log.error("Network error accessing Bitfinex.");
                Tool.sleepFor(1000);
            }
        }
        running=false;
    }

    public void logon() {
        accountId = properties.get("externalAccount");
//        secret = properties.get("secret");
        thread.start();
    }


    private String getInstrumentString() {
        StringBuilder b = new StringBuilder();
        for(String symbol : mapPidToSymbol.values()) {
            b.append(symbol);
            b.append(",");
        }
        return b.toString();
    }

    private void receive() throws IOException {

        for(String symbol : mapPidToSymbol.values()) {
            String pid = mapSymbolToPid.get(symbol);

            try {
                BitfinexDepth bitfinexDepth = ((BitfinexMarketDataServiceRaw)marketDataService).getBitfinexOrderBook(symbol, 50, 50);
                System.out.println("Current Order Book size for BTC / USD: " + (bitfinexDepth.getAsks().length + bitfinexDepth.getBids().length));

                System.out.println("First Ask: " + bitfinexDepth.getAsks()[0].toString());

                System.out.println("First Bid: " + bitfinexDepth.getBids()[0].toString());

                double amount = 50;
                BigDecimal bid = getPriceForAmount(bitfinexDepth.getBids(), amount);
                BigDecimal ask = getPriceForAmount(bitfinexDepth.getAsks(), amount);

                System.out.println("bbo: "+bid.toString()+" : "+ask+" spread="+(ask.subtract(bid).toString()));

                PriceData data = new PriceData(Tool.getUTCTimestamp(),
                        pid, new Decimal(bid), new Decimal(ask), new Decimal(bid),
                        Decimal.fromDouble(amount), Decimal.fromDouble(amount), Decimal.ONE);

                priceDataConsumer.onPriceData(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

//    public static PriceFeedX createFromPropertiesReader(PropertiesReader prop) {
//        return new PriceFeedX(prop);
//    }

    public void shutdown() {
        shutdown = true;
        stopReceiving = true;
    }

    public XPriceFeed(ConcurrentHashMap<String, String> _mapPidToSymbol) {
        mapPidToSymbol = _mapPidToSymbol;
        subscriptionList = new ConcurrentHashMap<String, IConsumePriceData>();
        mapSymbolToPid = new ConcurrentHashMap<String, String>();
        thread = new Thread(this);
        stopReceiving = false;
        shutdown = false;
        running=true;

        exchange = ExchangeFactory.INSTANCE.createExchange(BitfinexExchange.class.getName());
        marketDataService = exchange.getPollingMarketDataService();

    }

    public void setProductProvider(IProvideProduct productProvider) {
        this.productProvider = productProvider;
    }


    //////////////////////////////////////////////////////////////////////////////////////

    private BigDecimal getPriceForAmount(BitfinexLevel[] book, double amount) {
        double sum=0.0;
        for(int i=0; i<book.length; i++) {
            sum+=book[i].getAmount().doubleValue();
            if(sum>=amount) {
                return book[i].getPrice();
            }
        }
        return book[book.length-1].getPrice();
    }


    private boolean running;
    private boolean stopReceiving;
    private boolean shutdown;
    private Thread thread;
    private String accountId;
//    private String secret;
    private IProvideProperties properties;
    private IProvideProduct productProvider;
    private ConcurrentHashMap<String, IConsumePriceData> subscriptionList;
    private ConcurrentHashMap<String, String> mapPidToSymbol;
    private ConcurrentHashMap<String, String> mapSymbolToPid;
    private IConsumePriceData priceDataConsumer;

    Exchange exchange;
    PollingMarketDataService marketDataService;
}
