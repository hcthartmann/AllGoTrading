package org.yats.trader.examples.server;

import org.yats.connectivity.xchange.X796PricePoll;
import org.yats.connectivity.xchange.XPricefeedServer;

/**
 * Created
 * Date: 11/04/15
 * Time: 13:00
 */
public class X796PricefeedMain
{
    public static void main(String args[]) throws Exception {

        String pricePollClassName = X796PricePoll.class.getSimpleName();

        XPricefeedServer q = new XPricefeedServer.Factory().createXPricefeedServerFromProperties(pricePollClassName);

        try {
            q.go();
        } catch (RuntimeException r)
        {
            r.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }
}
