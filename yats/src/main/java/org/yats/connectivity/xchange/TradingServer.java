package org.yats.connectivity.xchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.*;
import org.yats.connectivity.messagebus.ExchangeToBusConnection;
import org.yats.trading.IProvideTrading;
import org.yats.trading.IProvideTradingProvider;

import java.io.IOException;

/**
 * Created
 * Date: 12/04/15
 * Time: 12:50
 */

public class TradingServer
{
    public void go() throws InterruptedException, IOException
    {
        tradingConnection.start();
        Thread.sleep(500);
        exchangeToBusConnection.start();

        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        exchangeToBusConnection.shutdown();
        Thread.sleep(500);
        tradingConnection.shutdown();
        Thread.sleep(500);

        System.exit(0);
    }

    public static class Factory {
        public TradingServer createTradingServerFromPropertiesFile(String _tradingClassName) {
            String propName = Tool.getPersonalSubdirConfigFilename("config", "xchanges", _tradingClassName);
            PropertiesReader prop = PropertiesReader.createFromConfigFile(propName);

            IProvideTradingProvider factory = instantiateTradingFactory(_tradingClassName);
            IProvideTrading tradingProvider = factory.createFromProperties(prop);
            Mapping<String,String> mapTradablePid2XPid = prop.toMap();

            TradingConnection tradingConnection = new TradingConnection(mapTradablePid2XPid, tradingProvider);
            ExchangeToBusConnection exchangeToBusConnection = new ExchangeToBusConnection.Factory().create(prop, tradingConnection);

            return new TradingServer(exchangeToBusConnection, tradingConnection);
        }

        public static IProvideTradingProvider instantiateTradingFactory(String classname) {
            String defaultNestedFactoryName = "org.yats.connectivity.xchange."+classname+"$Factory";
            try {
                Class<?> c =Class.forName(defaultNestedFactoryName);
                return (IProvideTradingProvider) c.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            throw new CommonExceptions.CouldNotInstantiateClassException("Factory class "+defaultNestedFactoryName+" could not be created!");
        }

    }


    public TradingServer(ExchangeToBusConnection _exchangeToBusConnection, TradingConnection _tradingConnection) {
        exchangeToBusConnection = _exchangeToBusConnection;
        tradingConnection =_tradingConnection;
    }


    ////////////////////////////////////////////////////////////////////////////////



    private ExchangeToBusConnection exchangeToBusConnection;
    private TradingConnection tradingConnection;


    private final Logger log = LoggerFactory.getLogger(PricefeedServer.class);
}
