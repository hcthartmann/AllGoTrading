package org.yats.connectivity.xchange;



import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.bitfinex.v1.BitfinexExchange;
import com.xeiam.xchange.bitfinex.v1.dto.marketdata.BitfinexDepth;
import com.xeiam.xchange.bitfinex.v1.dto.marketdata.BitfinexLevel;
import com.xeiam.xchange.bitfinex.v1.service.polling.BitfinexMarketDataServiceRaw;
import com.xeiam.xchange.service.polling.marketdata.PollingMarketDataService;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created
 * Date: 01/04/15
 * Time: 17:26
 */

public class BitfinexConnection {

    public static void main(String args[])  {
        BitfinexConnection b = new BitfinexConnection();
        b.go();
    }


    public void go()
    {
        exchange = createExchange();
        PollingMarketDataService marketDataService = exchange.getPollingMarketDataService();

        try {
            BitfinexDepth bitfinexDepth = ((BitfinexMarketDataServiceRaw)marketDataService).getBitfinexOrderBook("btcusd", 50, 50);

            System.out.println("Current Order Book size for BTC / USD: " + (bitfinexDepth.getAsks().length + bitfinexDepth.getBids().length));

            System.out.println("First Ask: " + bitfinexDepth.getAsks()[0].toString());

            System.out.println("First Bid: " + bitfinexDepth.getBids()[0].toString());

            BigDecimal bid = getPriceForAmount(bitfinexDepth.getBids(), 50);
            BigDecimal ask = getPriceForAmount(bitfinexDepth.getAsks(), 50);

            System.out.println("bbo: "+bid.toString()+" : "+ask+" spread="+(ask.subtract(bid).toString()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BitfinexConnection() {
    }


    ////////////////////////////////////////////////////////////////////////////////

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


    private Exchange createExchange() {

        // Use the factory to get BFX exchange API using default settings
        Exchange bfx = ExchangeFactory.INSTANCE.createExchange(BitfinexExchange.class.getName());

//        ExchangeSpecification bfxSpec = bfx.getDefaultExchangeSpecification();
//
//        bfxSpec.setApiKey("s3RMuV7u4h8SU3Mu6afs2R48Oj9zLoeofHSdfRz5udm");
//        bfxSpec.setSecretKey("rPRFQ7JskGqs0FUDWYbOkHaO8mXejJrFYN34turQSj6");
//
//        bfx.applySpecification(bfxSpec);

        return bfx;
    }

    Exchange exchange;

}
