package org.yats.trader.examples.server;

import org.yats.connectivity.xchange.BitfinexPricePoll;
import org.yats.connectivity.xchange.PricefeedServer;

public class BitfinexPricefeedMain  {

    public static void main(String args[]) throws Exception {

        String pricePollClassName = BitfinexPricePoll.class.getSimpleName();

        PricefeedServer q = new PricefeedServer.Factory().createXPricefeedServerFromProperties(pricePollClassName);

        try {
            q.go();
        } catch (RuntimeException r)
        {
            r.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }

} // class
