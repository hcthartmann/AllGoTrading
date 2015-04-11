package org.yats.trading;

import org.yats.common.CommonExceptions;
import org.yats.common.Decimal;
import org.yats.common.Mapping;


public class PriceDataMap {

    public boolean containsKey(String productId) {
        return rates.containsKey(productId);
    }

    public Decimal getLastPrice(String productId) {
        return get(productId).getLast();
    }

    public PriceData get(String productId) {
        if(!containsKey(productId)) throw new CommonExceptions.KeyNotFoundException("Can not find key "+productId);
        return rates.get(productId);
    }

    public void put(String productId, PriceData priceData) {
        rates.put(productId, priceData);
    }

    public PriceDataMap() {
        rates = new Mapping<String, PriceData>();
    }

    private Mapping<String, PriceData> rates;


} // class
