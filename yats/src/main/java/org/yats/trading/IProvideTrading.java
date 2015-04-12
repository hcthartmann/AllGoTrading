package org.yats.trading;


import org.yats.common.Decimal;
import org.yats.common.Mapping;

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

        public List<OpenOrder> getOpenOrderList();
}
