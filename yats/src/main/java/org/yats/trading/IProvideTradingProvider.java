package org.yats.trading;

import org.yats.common.PropertiesReader;

/**
 * Created
 * Date: 12/04/15
 * Time: 13:28
 */
public interface IProvideTradingProvider
{
    public IProvideTrading createFromProperties(PropertiesReader prop);
}
