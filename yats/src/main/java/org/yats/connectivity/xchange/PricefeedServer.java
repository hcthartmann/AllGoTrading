package org.yats.connectivity.xchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.CommonExceptions;
import org.yats.common.IProvidePriceDataProvider;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.connectivity.messagebus.PricefeedToBusConnection;
import org.yats.trading.IProvidePriceData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created
 * Date: 09/04/15
 * Time: 19:42
 */
public class PricefeedServer
{


    public void go() throws InterruptedException, IOException
    {
        priceFeed.start();
        Thread.sleep(500);
        pricefeedToBusConnection.start();

        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        pricefeedToBusConnection.shutdown();
        Thread.sleep(500);
        priceFeed.shutdown();
        Thread.sleep(500);

        System.exit(0);
    }

    public static class Factory {
        public PricefeedServer createXPricefeedServerFromProperties(String _pricePollClassName) {
            String propName = Tool.getPersonalSubdirConfigFilename("config", "xchanges", _pricePollClassName);
            PropertiesReader prop = PropertiesReader.createFromConfigFile(propName);
            IProvidePriceDataProvider factory = instantiatePricePollFactory(_pricePollClassName);
            IProvidePriceData priceDataProvider = factory.createFromProperties(prop);
            List<String> subscribableProductIds = new ArrayList<String>(prop.getKeySet());

            Pricefeeder pricefeed = new Pricefeeder(subscribableProductIds, priceDataProvider);
            PricefeedToBusConnection pricefeedToBusConnection = new PricefeedToBusConnection.Factory().create(prop, pricefeed);

            return new PricefeedServer(pricefeedToBusConnection, pricefeed);
        }

        public static IProvidePriceDataProvider instantiatePricePollFactory(String classname) {
            String defaultNestedFactoryName = "org.yats.connectivity.xchange."+classname+"$Factory";
            try {
                Class<?> c =Class.forName(defaultNestedFactoryName);
                return (IProvidePriceDataProvider) c.newInstance();
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


    public PricefeedServer(PricefeedToBusConnection _pricefeedToBusConnection, Pricefeeder _priceFeed) {
        pricefeedToBusConnection = _pricefeedToBusConnection;
        priceFeed=_priceFeed;
    }


    ////////////////////////////////////////////////////////////////////////////////



    private PricefeedToBusConnection pricefeedToBusConnection;
    private Pricefeeder priceFeed;


    private final Logger log = LoggerFactory.getLogger(PricefeedServer.class);

}
