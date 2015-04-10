package org.yats.trader.examples.server;

import org.yats.connectivity.xchange.BitfinexPricepoll;
import org.yats.connectivity.xchange.XPriceFeedServer;

public class BitfinexPricefeedMain  {

    public static void main(String args[]) throws Exception {

        String pricePollClassName = BitfinexPricepoll.class.getSimpleName();

        XPriceFeedServer q = new XPriceFeedServer.Factory().createXPricefeedServerFromProperties(pricePollClassName);

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
