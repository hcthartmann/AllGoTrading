package org.yats.connectivity.xchange;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.bitfinex.v1.BitfinexExchange;

/**
 * Created
 * Date: 12/04/15
 * Time: 00:03
 */
public class BitfinexTrading
{


    public BitfinexTrading(Exchange exchange)
    {
        this.exchange = exchange;
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
