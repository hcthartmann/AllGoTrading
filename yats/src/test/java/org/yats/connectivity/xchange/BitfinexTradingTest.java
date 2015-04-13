package org.yats.connectivity.xchange;

import junit.framework.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.Mapping;
import org.yats.common.PropertiesReader;
import org.yats.trading.*;

/**
 * Created
 * Date: 2015-04-12
 * Time: 18:59
 */
public class BitfinexTradingTest {

    @Test(groups = { "internet" })
    public void canGetPriceDataFromBitfinex()
    {
        PriceData p = bfxFeed.getPriceData("BFX_XBTUSD");
        OfferBook book = p.getBook();
        Assert.assertTrue(book.getDepth(BookSide.BID) > 10);
        Assert.assertTrue(book.getDepth(BookSide.ASK) > 10);
    }

    @Test(groups = { "trading" })
    public void canLogin()
    {
        Assert.assertTrue(bfxTrading.login());
    }

    @Test(groups = { "trading" })
    public void canReceiveAssets()
    {
        Mapping<String, Decimal> map = bfxTrading.getAssets();
        Assert.assertTrue(map.containsKey("trading_usd"));
    }


    @Test(groups = { "trading" })
    public void canSendAndDeleteOrder()
    {
        PriceData p = bfxFeed.getPriceData("BFX_XBTUSD");
        Decimal price = p.getBid().multiply(Decimal.fromString("0.95"));
        OrderNew buy = OrderNew.create()
                .withBookSide(BookSide.BID)
                .withInternalAccount("bfx")
                .withLimit(price)
                .withSize(Decimal.fromString("0.1"))
                .withProductId(TestPriceData.PROD_BFX_XBTUSD)
                ;
        bfxTrading.updateReceipts();
        int ordersBefore = bfxTrading.getOpenOrderMap().size();
        Assert.assertTrue(ordersBefore==0);
        bfxTrading.sendOrderNew(buy);
        int ordersAfterSend = bfxTrading.getOpenOrderMap().size();
        Assert.assertTrue(ordersAfterSend==1);
        Receipt receipt = bfxTrading.getReceipt(buy.getOrderId());
        bfxTrading.sendOrderCancel(receipt.createOrderCancel());
        int ordersAfterCancel = bfxTrading.getOpenOrderMap().size();
        Assert.assertTrue(ordersAfterCancel==0);
    }


    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        String bfxPricePollName = BitfinexPricePoll.class.getSimpleName();
        PropertiesReader bfxPriceProp = PropertiesReader.createFromPersonalSubdir("config", "xchanges", bfxPricePollName);
        bfxFeed = PricefeedServer.Factory.instantiatePricePollFactory(bfxPricePollName).createFromProperties(bfxPriceProp);

        String bfxTradingName = BitfinexTrading.class.getSimpleName();
        PropertiesReader bfxTradingProp = PropertiesReader.createFromPersonalSubdir("config", "xchanges", bfxTradingName);
        bfxTrading = new BitfinexTrading.Factory().createFromProperties(bfxTradingProp);
    }

    private IProvidePriceData bfxFeed;
    private BitfinexTrading bfxTrading;
}
