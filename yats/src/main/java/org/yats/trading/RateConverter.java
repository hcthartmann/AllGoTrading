package org.yats.trading;

import org.yats.common.UniqueId;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.ConcurrentHashMap;

public class RateConverter implements IConsumeMarketData {


    public Position convert(Position position, String targetProductId) {
        throw new NotImplementedException();
//        return new Position(targetProductId, Decimal.ZERO);
    }

    @Override
    public void onMarketData(MarketData marketData) {
        rates.put(marketData.getProductId(), marketData);
    }

    @Override
    public UniqueId getConsumerId() {
        return UniqueId.create();
    }

    public RateConverter() {
        rates = new ConcurrentHashMap<String, MarketData>();
    }


    ConcurrentHashMap<String, MarketData> rates;

} // class