package org.yats.connectivity.xchange;


import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.bitfinex.v1.BitfinexExchange;
import com.xeiam.xchange.bitfinex.v1.dto.marketdata.BitfinexDepth;
import com.xeiam.xchange.bitfinex.v1.dto.marketdata.BitfinexLevel;
import com.xeiam.xchange.bitfinex.v1.service.polling.BitfinexMarketDataServiceRaw;
import com.xeiam.xchange.exceptions.ExchangeException;
import com.xeiam.xchange.service.polling.marketdata.PollingMarketDataService;
import org.joda.time.DateTime;
import org.yats.common.Decimal;
import org.yats.common.PropertiesReader;
import org.yats.connectivity.ConnectivityExceptions;
import org.yats.trading.*;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created
 * Date: 08/04/15
 * Time: 12:56
 */

public class BitfinexPricefeed implements IProvidePriceData {

    @Override
    public PriceData getPriceData(String productId) {
        String xPid = mapPidToExchangeSymbol.get(productId);
        PollingMarketDataService marketDataService = bfxExchange.getPollingMarketDataService();
        try {
            BitfinexDepth bitfinexDepth = ((BitfinexMarketDataServiceRaw)marketDataService).getBitfinexOrderBook(xPid, 50, 50);

            if(bitfinexDepth.getBids().length<1 || bitfinexDepth.getAsks().length<1)
                throw new ConnectivityExceptions.UnexpectedExternalInputException("depth is empty!");
            Decimal bid = new Decimal(bitfinexDepth.getBids()[0].getPrice());
            Decimal ask = new Decimal(bitfinexDepth.getAsks()[0].getPrice());
            Decimal bidSize = new Decimal(bitfinexDepth.getBids()[0].getAmount());
            Decimal askSize = new Decimal(bitfinexDepth.getAsks()[0].getAmount());
            Decimal mid = ask.add(bid).divide(Decimal.TWO);
            PriceData p = new PriceData(DateTime.now(), productId, bid, ask, mid, bidSize, askSize, Decimal.fromString("0.01"));
            OfferBook book = convertToOfferBook(bitfinexDepth);
            p.setBook(book);
            return p;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectivityExceptions.ConnectionException(e.getMessage());
        }
    }

    public static class Factory {
        public BitfinexPricefeed createFromProperties(PropertiesReader prop) {
            Exchange bfxExchange = ExchangeFactory.INSTANCE.createExchange(BitfinexExchange.class.getName());
            BitfinexPricefeed bfxFeed = new BitfinexPricefeed(bfxExchange, prop.toMap());
            return bfxFeed;
        }
    }

    public BitfinexPricefeed(Exchange _bfxExchange, ConcurrentHashMap<String,String> _mapPidToExchangeSymbol) {
        bfxExchange = _bfxExchange;
        mapPidToExchangeSymbol = _mapPidToExchangeSymbol;
    }


    //////////////////////////////////////////////////////////////////////////////

    private OfferBook convertToOfferBook(BitfinexDepth bitfinexDepth) {
        OfferBookSide bids = new OfferBookSide(BookSide.BID);
        for(BitfinexLevel bidLine : bitfinexDepth.getBids()) {
            bids.add(new BookRow(new Decimal(bidLine.getAmount()), new Decimal(bidLine.getPrice())));
        }
        OfferBookSide asks = new OfferBookSide(BookSide.ASK);
        for(BitfinexLevel askLine : bitfinexDepth.getAsks()) {
            asks.add(new BookRow(new Decimal(askLine.getAmount()), new Decimal(askLine.getPrice())));
        }
        OfferBook book = new OfferBook(bids, asks);
        return book;
    }


    private Decimal getPriceForAmount(BitfinexLevel[] book, double amount) {
        double sum=0.0;
        for(int i=0; i<book.length; i++) {
            sum+=book[i].getAmount().doubleValue();
            if(sum>=amount) {
                return new Decimal(book[i].getPrice());
            }
        }
        return new Decimal(book[book.length-1].getPrice());
    }


    private Exchange bfxExchange;
    private ConcurrentHashMap<String,String> mapPidToExchangeSymbol;


}
