package org.yats.common;

import org.yats.trading.IProvidePriceData;

/**
 * Created
 * Date: 09/04/15
 * Time: 15:12
 */
public interface IProvidePriceDataProvider {
    public IProvidePriceData createFromProperties(PropertiesReader prop);
}
