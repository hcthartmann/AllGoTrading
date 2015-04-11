package org.yats.connectivity.x796;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.yats.common.Decimal;
import org.yats.common.Mapping;
import org.yats.connectivity.ConnectivityExceptions;
import org.yats.trading.*;

import java.io.IOException;


/*
* API description:
* https://796.com/other/apiV3.html?type=futures&op=weekly
*
*/

public class PriceFeed796 implements IProvidePriceData
{

    @Override
    public PriceData getPriceData(String productId)
    {
        if(!mapPid2XPid.containsKey(productId)) throw new ConnectivityExceptions.UnknownIdException("Unknown:"+productId);

        try {
            OfferBook book = getDepth();
            if(book.isAnyBookSideEmpty()) throw new ConnectivityExceptions.ConnectionException("received empty book!");

            Decimal bid = book.getBookRow(BookSide.BID, 0).getPrice();
            Decimal ask = book.getBookRow(BookSide.ASK, 0).getPrice();
            Decimal mid = bid.add(ask).divide(Decimal.TWO);

            PriceData priceData = new PriceData(
                    DateTime.now(),
                    "X796_XBTUSD",
                    bid,
                    ask,
                    mid,
                    book.getBookRow(BookSide.BID, 0).getSize(),
                    book.getBookRow(BookSide.ASK, 0).getSize(),
                    Decimal.fromString("0.01"));
            priceData.setBook(book);
            return priceData;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectivityExceptions.ConnectionException(e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
            throw new ConnectivityExceptions.ConnectionException(e.getMessage());
        }
    }


    public PriceFeed796(Mapping<String,String> _mapPid2XPid) {
        mapPid2XPid=_mapPid2XPid;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public OfferBook getDepth() throws IOException, JSONException {
        OfferBook offerBook = new OfferBook();

        JSONObject responseData = ApiRequest.processHTTPGetRequest(ApiRequest.DEPTH_API_URL);

        JSONArray asksData = responseData.getJSONArray("asks");
        JSONArray bidsData = responseData.getJSONArray("bids");

        // Get the ask data and add it to the OfferBook
        for (int i = asksData.length() - 1; i >= 0; i--) {
            JSONArray askData = asksData.getJSONArray(i);
            BookRow askBookRow = new BookRow(askData.getString(1), askData.getString(0));

            offerBook.addAsk(askBookRow);
        }

        // Get the bid data and add it to the OfferBook
        for (int i = 0; i < bidsData.length(); i++) {
            JSONArray bidData = bidsData.getJSONArray(i);
            BookRow bidBookRow = new BookRow(bidData.getString(1), bidData.getString(0));

            offerBook.addBid(bidBookRow);
        }

        return offerBook;
    }

    private Mapping<String,String> mapPid2XPid;

}
