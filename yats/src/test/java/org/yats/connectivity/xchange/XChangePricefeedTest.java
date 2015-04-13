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

public class XChangePricefeedTest {

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
        String btccPricePollName = BtcchinaPricePoll.class.getSimpleName();
        String btccPropName = Tool.getPersonalSubdirConfigFilename("config", "xchanges", btccPricePollName);
        PropertiesReader btccProp = PropertiesReader.createFromConfigFile(btccPropName);
        btccFeed = PricefeedServer.Factory.instantiatePricePollFactory(btccPricePollName).createFromProperties(btccProp);

    }


    ///////////////////////////////////////////////////////////////




    private IProvidePriceData btccFeed;


}
