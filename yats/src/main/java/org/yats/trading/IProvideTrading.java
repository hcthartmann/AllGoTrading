package org.yats.trading;


import org.yats.common.Decimal;
import org.yats.common.Mapping;
import org.yats.common.UniqueId;

import java.util.List;

/**
 * Created
 * Date: 12/04/15
 * Time: 00:15
 */

public interface IProvideTrading extends IConsumeOrders
{
        public void login();

        public Mapping<String, Decimal> getAssets();

        public void cancelAllOrders();

        public void updateReceipts();
        public Receipt getReceipt(UniqueId orderId);
        public Mapping<String,Receipt> getOpenOrderMap();
}
