package org.yats.trading;

import junit.framework.Assert;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.connectivity.general.LastPriceServer;

/**
 * Created
 * Date: 10/04/15
 * Time: 13:46
 */
public class LastPriceServerTest
{
    @Test(groups = { "inMemory" })
    public void when_OnPriceData_receivedPriceData_getLatest_returnsIt()
    {
        lastPriceServer.onPriceData(newData);
        Assert.assertTrue(lastPriceServer.getLatest(testPid).getBid().isEqualTo(Decimal.fromString("2.22")));
    }

    @Test(groups = { "inMemory" })
    public void when_OnPriceData_receivesOlderPriceDataThanAlreadyInCache_getLatest_stillReturnsTheNewestData()
    {
        lastPriceServer.onPriceData(newData);
        lastPriceServer.onPriceData(oldData);
        Assert.assertTrue(lastPriceServer.getLatest(testPid).getBid().isEqualTo(Decimal.fromString("2.22")));
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        lastPriceServer = new LastPriceServer(null, null, null, null);
        oldData = new PriceData(DateTime.now(), testPid, Decimal.fromString("1.11"), null, null, null, null, null);
        newData = new PriceData(DateTime.now().plusMillis(100), testPid, Decimal.fromString("2.22"), null, null, null, null, null);
    }

    PriceData oldData;
    PriceData newData;
    private String testPid ="testPid";
    private LastPriceServer lastPriceServer;
}
