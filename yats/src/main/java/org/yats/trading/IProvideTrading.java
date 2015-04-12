package org.yats.trading;

import org.yats.common.Decimal;
import org.yats.common.Mapping;

import java.util.List;

/**
 * Created
 * Date: 12/04/15
 * Time: 00:15
 */

public interface IProvideTrading
{

        public void login();

        public Mapping<String, Decimal> getAssets();

        public void sendOrderNew(OrderNew newOrder);

        public void sendOrderCancel(OrderCancel o);

        public void cancelAllOrders();

        public List<OrderInMarket> getOpenOrderList();

}
