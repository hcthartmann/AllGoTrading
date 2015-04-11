package org.yats.connectivity.x796;

import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.Mapping;
import org.yats.trading.BookSide;
import org.yats.trading.OfferBook;

import java.io.IOException;

public class PriceFeed796Test {

    @Test(groups = { "internet" })
    public void canGetDepth() throws IOException, JSONException {
        OfferBook book = priceFeed.getDepth();
        Assert.assertFalse(book == null);
        Assert.assertFalse(book.isBookSideEmpty(BookSide.BID));
        Assert.assertFalse(book.isBookSideEmpty(BookSide.ASK));
        Assert.assertTrue(book.getDepth(BookSide.BID) > 5);
        Assert.assertTrue(book.getDepth(BookSide.ASK) > 5);

        // descending order in orderbook bid side
        Decimal bid0 = book.getRow(BookSide.BID, 0).getPrice();
        Decimal bid1 = book.getRow(BookSide.BID, 1).getPrice();
        Decimal bid2 = book.getRow(BookSide.BID, 2).getPrice();
        Assert.assertTrue(bid0.isGreaterThan(bid1));
        Assert.assertTrue(bid1.isGreaterThan(bid2));

        // ascending order in orderbook as side
        Decimal ask0 = book.getRow(BookSide.ASK, 0).getPrice();
        Decimal ask1 = book.getRow(BookSide.ASK, 1).getPrice();
        Decimal ask2 = book.getRow(BookSide.ASK, 2).getPrice();
        Assert.assertTrue(ask0.isLessThan(ask1));
        Assert.assertTrue(ask1.isLessThan(ask2));

        Decimal bidSize0 = book.getRow(BookSide.BID, 0).getSize();
        Decimal askSize2 = book.getRow(BookSide.ASK, 2).getSize();

        Assert.assertTrue(bidSize0.isGreaterThan(Decimal.ZERO));
        Assert.assertTrue(askSize2.isGreaterThan(Decimal.ZERO));
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp()
    {
        priceFeed = new PriceFeed796(new Mapping<String, String>());
    }

    /////////////////////////////////////////////////////////////////////

    private PriceFeed796 priceFeed;
}
