package org.yats.connectivity.x796;

import org.json.JSONException;
import org.json.JSONObject;
import org.yats.common.Decimal;
import org.yats.common.UniqueId;
import org.yats.connectivity.ConnectivityExceptions;
import org.yats.trading.BookSide;
import org.yats.trading.OrderCancel;
import org.yats.trading.OrderNew;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


/*
*
*
* API description:
*
* https://796.com/wiki.html
*
*
* API keys: to be provided
*
* Server Authorization
* Visit “User Panel” –> “Account security” -> “Trade API”,
* click to create API key, enter the name of APP, and then you’ll get APPID, APIKEY, Secret Key.
*
*
* APP name:	SECOND_KEY
* APPID:	11370
* API Key:	ef1c64bd-4fac-1bd7-dd26-7fb1-fe09adbd
* Secret Key: GQB6aBgl2bkNqo4qKQ2dkW5HEh6DFJ0F5pwOqxy8F/QClXmgGtVAfE11v+46
*
 */

public class TradeConnection796  {

    /*
     * tries to establish a connection to 796.com using
     * https://796.com/wiki/apiV2.html
     * and throws ConnectionException if authentication fails
     *
     * to verify the connection is working, retrieve
     * https://796.com/wiki/userV2.html?api=get_info
     * and verify username is 'ccc796'
     *
     */

    public void login() throws IOException, JSONException {
        Map<String, String> requestMap = new TreeMap<String, String>();
        requestMap.put("apikey", key);
        requestMap.put("secretkey", secret);

        // Compute the signature and add it to the request
        String signature = ApiRequest.buildSignature(requestMap, secret);
        requestMap.put("sign", signature);

        // Remove the secret key from the request
        requestMap.remove("secretkey");

        JSONObject responseData = ApiRequest.processHTTPPostRequest(ApiRequest.GET_INFO_API_URL, requestMap);

        try {
            // Load the data from the Response
            String receivedUsername = responseData.getJSONObject("data").getString("username");
            String email = responseData.getJSONObject("data").getString("email");

            if (!receivedUsername.equals(userName))
                throw new ConnectivityExceptions.ConnectionException("Unexpected username!");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new ConnectivityExceptions.ConnectionException("Login to 796 failed!");
        }
    }

    /*
    * returns the assets of the account as described in https://796.com/wiki/userV2.html?api=get_balance
    * example return:
    * {"errno":"0","msg":"success","data":{"btc":{"mainwallet":"40.8329425","marginaccount":"17.4546692",
    * "unmatched":41.56,"totalassets":99.9476117,"netassets":99.89966714},"ltc":{"mainwallet":"50",
    * "marginaccount":"50","unmatched":"0","totalassets":100,"netassets":100}}}
    *
    * the returned hashmap should have keys consisting of strings like "btc_mainwallet" and the value of that wallet
    * example:
    * btc_mainwallet=40.8329425
    * ltc_marginaccount=50
    *
     */
    public Map<String, Decimal> getAssets() throws IOException, JSONException {
        Map<String, Decimal> assetsMap = new TreeMap<String, Decimal>();

        Map<String, String> requestMap = new TreeMap<String, String>();
        requestMap.put("apikey", key);
        requestMap.put("secretkey", secret);

        // Compute the signature and add it to the request
        String signature = ApiRequest.buildSignature(requestMap, secret);
        requestMap.put("sign", signature);

        // Remove the secret key from the request
        requestMap.remove("secretkey");

        JSONObject responseData = ApiRequest.processHTTPPostRequest(ApiRequest.GET_ASSETS_API_URL, requestMap);
        JSONObject dataJson = responseData.getJSONObject("data");

        // Add the data for BTC
        JSONObject btcDataJson = dataJson.getJSONObject("btc");
        Iterator keys = btcDataJson.keys();

        while (keys.hasNext()) {
            String keyString = "" + keys.next();
            String assetKey = "btc_" + keyString;
            Decimal assetValue = new Decimal(btcDataJson.getString(keyString));
            assetsMap.put(assetKey, assetValue);
        }

        return assetsMap;
    }

    /*
    * sends the order to the market. on error throws one of the TradingExceptions
    * relevant API calls are
    * https://796.com/wiki/futuresV2.html?type=weeklyfutures&api=open_buy
    * https://796.com/wiki/futuresV2.html?type=weeklyfutures&api=open_sell
    * depending on OrderNew.BookSide
    *
    */
    public void sendOrderNew(OrderNew newOrder) throws IOException, JSONException {
        if (newOrder.getBookSide().equals(BookSide.ASK)) {
            Map<String, String> requestMap = new TreeMap<String, String>();
            requestMap.put("apikey", key);
            requestMap.put("secretkey", secret);
            requestMap.put("times", "10");
            requestMap.put("ptype", "A");
            requestMap.put("sell_num", newOrder.getSize().toString());
            requestMap.put("sell_price", newOrder.getLimit().toString());

            // Compute the signature and add it to the request
            String signature = ApiRequest.buildSignature(requestMap, secret);
            requestMap.put("sign", signature);

            // Remove the secret key from the request
            requestMap.remove("secretkey");

            JSONObject responseData = ApiRequest.processHTTPPostRequest(ApiRequest.SELL_ORDER_API_URL, requestMap);
            JSONObject dataJson = responseData.getJSONObject("data");

            if (responseData.getInt("errno") == 0) {
                String orderId = dataJson.getString("no");
                // Set the orderId
                newOrder.setOrderId(UniqueId.createFromString(orderId));
                System.out.println(responseData.getString("msg"));
            } else {
                System.out.println("Order failed.");
            }
        }

        if (newOrder.getBookSide().equals(BookSide.BID)) {
            Map<String, String> requestMap = new TreeMap<String, String>();
            requestMap.put("apikey", key);
            requestMap.put("secretkey", secret);
            requestMap.put("times", "10");
            requestMap.put("ptype", "A");
            requestMap.put("buy_num", newOrder.getSize().toString());
            requestMap.put("buy_price", newOrder.getLimit().toString());

            // Compute the signature and add it to the request
            String signature = ApiRequest.buildSignature(requestMap, secret);
            requestMap.put("sign", signature);

            // Remove the secret key from the request
            requestMap.remove("secretkey");

            JSONObject responseData = ApiRequest.processHTTPPostRequest(ApiRequest.BUY_ORDER_API_URL, requestMap);
            JSONObject dataJson = responseData.getJSONObject("data");

            if (responseData.getInt("errno") == 0) {
                String orderId = dataJson.getString("no");
                // Set the orderId
                newOrder.setOrderId(UniqueId.createFromString(orderId));
                System.out.println(responseData.getString("msg"));
            } else {
                System.out.println("Order failed.");
            }
        }
    }

    /*
    * cancels an order in the market. on error throws one of the TradingExceptions
    * relevant API calls are
    * https://796.com/wiki/futuresV2.html?type=weeklyfutures&api=cancel_order
    * depending on OrderNew.BookSide
    *
    */
    public void sendOrderCancel(OrderCancel orderCancel) throws IOException, JSONException {
        Map<String, String> requestMap = new TreeMap<String, String>();
        requestMap.put("apikey", key);
        requestMap.put("secretkey", secret);

        if (orderCancel.getBookSide().equals(BookSide.ASK)) {
            requestMap.put("bs", "sell");
        }

        if (orderCancel.getBookSide().equals(BookSide.BID)) {
            requestMap.put("bs", "buy");
        }

        requestMap.put("no", orderCancel.getOrderIdString());

        // Compute the signature and add it to the request
        String signature = ApiRequest.buildSignature(requestMap, secret);
        requestMap.put("sign", signature);

        // Remove the secret key from the request
        requestMap.remove("secretkey");

        JSONObject responseData = ApiRequest.processHTTPPostRequest(ApiRequest.CANCEL_ORDER_API_URL, requestMap);

        if (responseData.getInt("errno") == 0) {
            System.out.println(responseData.getString("msg"));
        } else {
            System.out.println("Cancel order failed.");
        }
    }

    public void cancelAll(OrderCancel orderCancel) throws IOException, JSONException {
        Map<String, String> requestMap = new TreeMap<String, String>();
        requestMap.put("apikey", key);
        requestMap.put("secretkey", secret);

        if (orderCancel.getBookSide().equals(BookSide.ASK)) {
            requestMap.put("bs", "sell");
        }

        if (orderCancel.getBookSide().equals(BookSide.BID)) {
            requestMap.put("bs", "buy");
        }

        requestMap.put("no", orderCancel.getOrderIdString());

        // Compute the signature and add it to the request
        String signature = ApiRequest.buildSignature(requestMap, secret);
        requestMap.put("sign", signature);

        // Remove the secret key from the request
        requestMap.remove("secretkey");

        JSONObject responseData = ApiRequest.processHTTPPostRequest(ApiRequest.CANCEL_ALL_ORDERS_API_URL, requestMap);

        if (responseData.getInt("errno") == 0) {
            System.out.println(responseData.getString("msg"));
        } else {
            System.out.println("Cancel orders failed.");
        }
    }

//    /*
//     * get a list of open orders in market using
//     * https://796.com/wiki/futuresV2.html?type=weeklyfutures&api=orders
//     *
//     */
//    @Override
//    public List<OrderInMarket> getOpenOrderList() throws IOException, JSONException {
//        List<OrderInMarket> orderList = new ArrayList<OrderInMarket>();
//
//        Map<String, String> requestMap = new TreeMap<String, String>();
//        requestMap.put("apikey", key);
//        requestMap.put("secretkey", secret);
//
//        // Compute the signature and add it to the request
//        String signature = ApiRequest.buildSignature(requestMap, secret);
//        requestMap.put("sign", signature);
//
//        // Remove the secret key from the request
//        requestMap.remove("secretkey");
//
//        JSONObject responseData = ApiRequest.processHTTPPostRequest(ApiRequest.GET_ORDERS_API_URL, requestMap);
//        JSONArray dataJson = responseData.getJSONArray("data");
//
//        for (int i = 0; i < dataJson.length(); i++) {
//            String bookSideString = dataJson.getJSONObject(i).getString("bs");
//            String orderId = dataJson.getJSONObject(i).getString("no");
//
//            Decimal limit = new Decimal(dataJson.getJSONObject(i).getString("price"));
//            Decimal size = new Decimal(dataJson.getJSONObject(i).getString("gnum"));
//
//            Double sizeAmount = new Double(dataJson.getJSONObject(i).getString("gnum"));
//            Double transactionAmount = new Double(dataJson.getJSONObject(i).getString("cjnum"));
//
//            BookSide side = BookSide.fromSideName(bookSideString);
//
//            OrderNew orderNew = new OrderNew()
//                    .withOrderId(UniqueId.createFromString(orderId))
//                    .withBookSide(side)
//                    .withLimit(limit)
//                    .withSize(size)
//                    .withProductId("X796_XBTUSDWeek")
//                    ;
//            orderList.add(new OrderInMarket(orderNew, Decimal.fromDouble(sizeAmount - transactionAmount)));
//        }
//
//        return orderList;
//    }

    public TradeConnection796(String _username, String _appId, String _key, String _secret) {
        userName = _username;
        appId = _appId;
        key = _key;
        secret = _secret;
    }

    ///////////////////////////////////////////////////////////////////////////////////


    private String userName;
    private String appId;
    private String key;
    private String secret;

}
