package org.yats.trader.examples.server;

import org.yats.connectivity.xchange.BtcchinaPricePoll;
import org.yats.connectivity.xchange.XPricefeedServer;

/**
 * Created
 * Date: 09/04/15
 * Time: 22:44
 */
public class BtcchinaPricefeedMain
{

    public static void main(String args[]) throws Exception {

        String pricePollClassName = BtcchinaPricePoll.class.getSimpleName();

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

} // class
