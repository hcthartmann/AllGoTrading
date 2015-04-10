package org.yats.connectivity.xchange;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.btcchina.BTCChinaExchange;
import com.xeiam.xchange.btcchina.dto.marketdata.BTCChinaDepth;
import com.xeiam.xchange.btcchina.service.polling.BTCChinaMarketDataServiceRaw;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.service.polling.marketdata.PollingMarketDataService;
import org.joda.time.DateTime;
import org.yats.common.Decimal;
import org.yats.common.IProvidePriceDataProvider;
import org.yats.common.PropertiesReader;
import org.yats.connectivity.ConnectivityExceptions;
import org.yats.trading.*;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created
 * Date: 09/04/15
 * Time: 22:46
 */
public class BtcchinaPricePoll implements IProvidePriceData
{

    @Override
    public PriceData getPriceData(String productId) {
//        String xPid = mapPidToExchangeSymbol.get(productId);
        CurrencyPair xPid = mapPidToXPid.get(productId);
        PollingMarketDataService marketDataService = exchange.getPollingMarketDataService();
        try {
            OrderBook orderBook = marketDataService.getOrderBook(xPid, 50);


            if(orderBook.getBids().size()<1 || orderBook.getAsks().size()<1)
                throw new ConnectivityExceptions.UnexpectedExternalInputException("depth is empty!");
            Decimal bid = new Decimal(orderBook.getBids().get(0).getLimitPrice());
            Decimal ask = new Decimal(orderBook.getAsks().get(0).getLimitPrice());
            Decimal bidSize = new Decimal(orderBook.getBids().get(0).getTradableAmount());
            Decimal askSize = new Decimal(orderBook.getAsks().get(0).getTradableAmount());
            Decimal mid = ask.add(bid).divide(Decimal.TWO);
            PriceData p = new PriceData(DateTime.now(), productId, bid, ask, mid, bidSize, askSize, Decimal.fromString("0.01"));
            OfferBook book = convertToOfferBook(orderBook);
            p.setBook(book);
            return p;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectivityExceptions.ConnectionException(e.getMessage());
        }
    }

    public static class Factory implements IProvidePriceDataProvider
    {
        public Factory() {}
        @Override
        public IProvidePriceData createFromProperties(PropertiesReader prop) {
            Exchange exchange = ExchangeFactory.INSTANCE.createExchange(BTCChinaExchange.class.getName());
            BtcchinaPricePoll feed = new BtcchinaPricePoll(exchange, prop.toMap());
            return feed;
        }
    }

    public BtcchinaPricePoll(Exchange _exchange, ConcurrentHashMap<String, String> _mapPidToExchangeSymbol) {
        exchange = _exchange;
        mapPidToExchangeSymbol = _mapPidToExchangeSymbol;
        mapPidToXPid = new ConcurrentHashMap<String, CurrencyPair>();
        mapPidToXPid.put("BTCC_XBTCNY", CurrencyPair.BTC_CNY);
    }



    //////////////////////////////////////////////////////////////////////////////

    private OfferBook convertToOfferBook(OrderBook orderBook) {
        OfferBookSide bids = new OfferBookSide(BookSide.BID);
        for(LimitOrder bidLine : orderBook.getBids()) {
            bids.add(new BookRow(new Decimal(bidLine.getTradableAmount()), new Decimal(bidLine.getLimitPrice())));
        }
        OfferBookSide asks = new OfferBookSide(BookSide.ASK);
        for(LimitOrder askLine : orderBook.getAsks()) {
            asks.add(new BookRow(new Decimal(askLine.getTradableAmount()), new Decimal(askLine.getLimitPrice())));
        }
        OfferBook book = new OfferBook(bids, asks);
        return book;
    }


    private Exchange exchange;
    private ConcurrentHashMap<String,String> mapPidToExchangeSymbol;
    private ConcurrentHashMap<String,CurrencyPair> mapPidToXPid;
}
