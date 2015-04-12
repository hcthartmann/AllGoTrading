package org.yats.trader.examples.server;

import org.yats.connectivity.xchange.BitfinexTrading;
import org.yats.connectivity.xchange.TradingServer;

/**
 * Created
 * Date: 12/04/15
 * Time: 12:49
 */
public class BitfinexTradingMain
{
    public static void main(String args[]) throws Exception {

        String tradingClassName = BitfinexTrading.class.getSimpleName();

        TradingServer q = new TradingServer.Factory().createTradingServerFromPropertiesFile(tradingClassName);

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
