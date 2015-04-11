package org.yats.trading;

import junit.framework.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;

public class OfferBookTest {

    @Test(groups = { "inMemory" })
    public void canConvertToAndFromCSV() {
        String csv = book.toStringCSV();
        OfferBook csvBook = OfferBook.fromStringCSV(csv);
        String csv2 = csvBook.toStringCSV();
        assert(csv.compareTo(csv2)==0);
    }

    @Test(groups = { "inMemory" })
    public void canGetSingleRowsFromBook() {
        assert(book.getDepth(BookSide.BID)==2);
        assert(book.getDepth(BookSide.ASK)==0);
        assert(book.getBookRow(BookSide.BID, 0).isPrice(Decimal.fromString("22")));
        assert(book.getBookRow(BookSide.BID, 0).isSize(Decimal.fromString("10")));
        assert(book.getBookRow(BookSide.BID, 1).isPrice(Decimal.fromString("23")));
        assert(book.getBookRow(BookSide.BID, 1).isSize(Decimal.fromString("11")));
    }

    @Test(groups = { "inMemory" })
    public void whenSameBookGiven_isSameBestBookRowsAs_returnsTrue() {
        boolean same = aBook.isSameBestRowsAs(aBook,5);
        Assert.assertTrue(same);
    }

    @Test(groups = { "inMemory" })
    public void whenBookWithSameValuesGiven_isSameBestBookRowsAs_returnsTrue() {
        boolean same = aBook.isSameBestRowsAs(a2Book,5);
        Assert.assertTrue(same);
    }

    @Test(groups = { "inMemory" })
    public void whenDifferentBookGiven_isSameBestBookRowsAs_returnsFalse() {
        boolean same = aBook.isSameBestRowsAs(bBook,5);
        Assert.assertFalse(same);
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        book = new OfferBook();
        book.addBid("10", "22");
        book.addBid("11", "23");

        aBook = new OfferBook();
        aBook.addBid("10", "22.3");
        aBook.addBid("11", "23.4");
        aBook.addAsk("1", "100.1");
        aBook.addAsk("2", "100.2");

        a2Book = new OfferBook();
        a2Book.addBid("10", "22.3");
        a2Book.addBid("11", "23.4");
        a2Book.addAsk("1", "100.1");
        a2Book.addAsk("2", "100.2");

        bBook = new OfferBook();
        bBook.addBid("10", "22.3");
        bBook.addBid("11", "23.4");
        bBook.addAsk("1", "100.1");
        bBook.addAsk("2", "100.999");


    }

    private OfferBook book;
    private OfferBook aBook;
    private OfferBook a2Book;
    private OfferBook bBook;

} // class
