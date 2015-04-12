package org.yats.connectivity.xchange;

import junit.framework.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.*;
import org.yats.trading.BookSide;
import org.yats.trading.IProvidePriceData;
import org.yats.trading.OfferBook;
import org.yats.trading.PriceData;

/**
 * Created
 * Date: 06/04/15
 * Time: 22:28
 */

public class XChangeTest {

    @Test(groups = { "internet" })
    public void canGetPriceDataFromBitfinex()
    {
        PriceData p = bfxFeed.getPriceData("BFX_XBTUSD");
        OfferBook book = p.getBook();
        Assert.assertTrue(book.getDepth(BookSide.BID)>10);
        Assert.assertTrue(book.getDepth(BookSide.ASK)>10);
    }

    @Test(groups = { "internet" })
    public void canGetPriceDataFromBtcchina()
    {
        PriceData p = btccFeed.getPriceData("BTCC_XBTCNY");
        OfferBook book = p.getBook();
        Assert.assertTrue(book.getDepth(BookSide.BID)>10);
        Assert.assertTrue(book.getDepth(BookSide.ASK)>10);
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        String bfxPricePollName = BitfinexPricePoll.class.getSimpleName();
        String bfxPropName = Tool.getPersonalSubdirConfigFilename("config", "xchanges", bfxPricePollName);
        PropertiesReader bfxProp = PropertiesReader.createFromConfigFile(bfxPropName);
        bfxFeed = PricefeedServer.Factory.instantiatePricePollFactory(bfxPricePollName).createFromProperties(bfxProp);

        String btccPricePollName = BtcchinaPricePoll.class.getSimpleName();
        String btccPropName = Tool.getPersonalSubdirConfigFilename("config", "xchanges", btccPricePollName);
        PropertiesReader btccProp = PropertiesReader.createFromConfigFile(btccPropName);
        btccFeed = PricefeedServer.Factory.instantiatePricePollFactory(btccPricePollName).createFromProperties(btccProp);

    }


    ///////////////////////////////////////////////////////////////


    private IProvidePriceData bfxFeed;

    private IProvidePriceData btccFeed;


}
