package org.yats.trading;

import org.yats.common.Decimal;

public class OrderInMarket {


    public OrderNew getInitialOrder() {
        return initialOrder;
    }

    public Decimal getRemainingSize() {
        return remainingSize;
    }

    public OrderInMarket(OrderNew _initialOrder, Decimal _remaingSize) {
        initialOrder =_initialOrder;
        remainingSize=_remaingSize;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private OrderNew initialOrder;
    private Decimal remainingSize;

}
