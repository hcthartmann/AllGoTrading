package org.yats.connectivity.x796;

import junit.framework.Assert;
import org.json.JSONException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;

import java.io.IOException;
import java.util.Map;


public class TradeConnection796Test {

    @Test(groups = { "internet" })
    public void canLoginTo796() throws IOException, JSONException
    {
        trade796.login();
    }

    @Test(groups = { "internet" })
    public void canGetAssets() throws IOException, JSONException {
        Map<String, Decimal> assets = trade796.getAssets();
        Assert.assertTrue(assets.containsKey("btc_marginaccount"));
//        Assert.assertTrue(assets.get("btc_marginaccount").isGreaterThan(Decimal.ZERO));
    }

//    @Test(groups = { "sendsOrders" })
//    public void canSendOrdersAndCancelThem() throws IOException, JSONException {
//        OrderNew order1 = new OrderNew()
//                .withBookSide(BookSide.BID)
//                .withLimit(Decimal.fromDouble(100))
//                .withSize(Decimal.fromDouble(0.1))
//                .withProductId("X796_XBTUSDWeek")
//        ;
//        OrderNew order2 = new OrderNew()
//                .withBookSide(BookSide.BID)
//                .withLimit(Decimal.fromDouble(101))
//                .withSize(Decimal.fromDouble(0.15))
//                .withProductId("X796_XBTUSDWeek")
//        ;
//        OrderNew order3 = new OrderNew()
//                .withBookSide(BookSide.ASK)
//                .withLimit(Decimal.fromDouble(301))
//                .withSize(Decimal.fromDouble(0.11))
//                .withProductId("X796_XBTUSDWeek")
//        ;
//
//        trade796.sendOrderNew(order1);
//        trade796.sendOrderNew(order2);
//
//        List<OrderInMarket> orderList = trade796.getOpenOrderList();
//
//        // check that the above orders made it into the market
//
//        Assert.assertTrue(orderList.size()==2);
//        Assert.assertTrue(orderList.get(0).getRemainingSize().isGreaterThan(Decimal.fromDouble(0.001)));
//        Assert.assertTrue(orderList.get(1).getRemainingSize().isLessThan(Decimal.fromDouble(0.2)));
//        Assert.assertTrue(orderList.get(0).getInitialOrder().getLimit().isLessThan(Decimal.fromDouble(200)));
//        Assert.assertTrue(orderList.get(1).getInitialOrder().getLimit().isGreaterThan(Decimal.fromDouble(99)));
//
//        // cancel the orders and check that they are indeed gone
//
//        trade796.sendOrderCancel(OrderCancel.fromOrderNew(orderList.get(0).getInitialOrder()));
//
//        List<OrderInMarket> orderListAfterFirstCancel = trade796.getOpenOrderList();
//        Assert.assertTrue(orderListAfterFirstCancel.size()==1);
//
//        trade796.sendOrderNew(order3);
//        trade796.sendOrderCancel(OrderCancel.fromOrderNew(orderListAfterFirstCancel.get(0).getInitialOrder()));
//
//        List<OrderInMarket> orderListAfterSecondCancel = trade796.getOpenOrderList();
//        Assert.assertTrue(orderListAfterSecondCancel.size()==1);
//        Assert.assertTrue(orderListAfterSecondCancel.get(0).getInitialOrder().getBookSide()==BookSide.ASK);
//
//        // getting rid of the ASK order...
//        trade796.sendOrderCancel(OrderCancel.fromOrderNew(orderListAfterSecondCancel.get(0).getInitialOrder()));
//
//        List<OrderInMarket> orderListAfterThirdCancel = trade796.getOpenOrderList();
//        Assert.assertTrue(orderListAfterThirdCancel.size()==0);
//
//    }


    @BeforeMethod(alwaysRun = true)
    public void setUp()
    {
        String className = TradeConnection796Test.class.getSimpleName();
        String propName = Tool.getPersonalSubdirConfigFilename("config", "xchanges", className);
        PropertiesReader prop = PropertiesReader.createFromConfigFile(propName);
        trade796 = new TradeConnection796(
                prop.get("USERNAME"),
                prop.get("APP_ID"),
                prop.get("API_KEY"),
                prop.get("SECRET_KEY")
        );
    }


    private TradeConnection796 trade796;
}
