package org.yats.trading;

/**
 * Created
 * Date: 2015-04-12
 * Time: 17:18
 */
public interface IConsumeOrders {
    public void sendOrderNew(OrderNew newOrder);
    public void sendOrderCancel(OrderCancel o);
}
