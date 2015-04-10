package org.yats.connectivity.x796;


import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiRequest {

    // ============================================================
    // Fields
    // ============================================================

    // API Data
    public static final String USERNAME = "ccc796";
    public static final String APP_ID = "11370";
    public static final String API_KEY = "ef1c64bd-4fac-1bd7-dd26-7fb1-fe09adbd";
    public static final String SECRET_KEY = "GQB6aBgl2bkNqo4qKQ2dkW5HEh6DFJ0F5pwOqxy8F/QClXmgGtVAfE11v+46";

    // API Methods
    public static final String TICKER_API_URL = "http://api.796.com/v3/futures/ticker.html?type=weekly";
    public static final String DEPTH_API_URL = "http://api.796.com/v3/futures/depth.html?type=weekly";
    public static final String GET_INFO_API_URL = "https://796.com/v2/user/get_info";
    public static final String GET_ASSETS_API_URL = "https://796.com/v2/user/get_assets";

    public static final String GET_ORDERS_API_URL = "https://796.com/v2/weeklyfutures/orders";
    public static final String BUY_ORDER_API_URL = "https://796.com/v2/weeklyfutures/open_buy";
    public static final String SELL_ORDER_API_URL = "https://796.com/v2/weeklyfutures/open_sell";
    public static final String CANCEL_ORDER_API_URL = "https://796.com/v2/weeklyfutures/cancel_order";
    public static final String CANCEL_ALL_ORDERS_API_URL = "https://796.com/v2/weeklyfutures/cancel_all";

    // ============================================================
    // Methods
    // ============================================================

    public static JSONObject processHTTPGetRequest(String apiUrl) throws IOException, JSONException {
        JSONObject responseJsonObject = new JSONObject();

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(apiUrl);

        HttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            responseJsonObject = new JSONObject(EntityUtils.toString(entity));
        }

        return responseJsonObject;
    }

    public static JSONObject processHTTPPostRequest(String apiUrl, Map<String, String> data) throws IOException, JSONException {
        JSONObject responseJsonObject = new JSONObject();

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(apiUrl);

        List<NameValuePair> dataParameters = new ArrayList<NameValuePair>();
        for (String key : data.keySet()) {
            dataParameters.add(new BasicNameValuePair(key, data.get(key)));
        }

        // Set the request data
        httpPost.setEntity(new UrlEncodedFormEntity(dataParameters));

        HttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            responseJsonObject = new JSONObject(EntityUtils.toString(entity));
        }

        return responseJsonObject;
    }

    // ============================================================
    // Utils
    // ============================================================

    public static String buildSignature(Map<String, String> elements, String secretKey) {
        String encodedSignature = "";
        String signature = "";

        for (String key : elements.keySet()) {
            signature = signature + key + "=" + elements.get(key) + "&";
        }

        // Remove the last value
        signature = signature.substring(0, signature.length() - 1);

        try {
            SecretKeySpec secret = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secret);

            byte[] digest = mac.doFinal(signature.getBytes());
            String digestString = "";

            for (byte b : digest) {
                digestString += String.format("%02x", b);
            }

            // Encode Signature
            encodedSignature = Base64.encodeBase64String(digestString.getBytes("CP1252"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encodedSignature;
    }
}
