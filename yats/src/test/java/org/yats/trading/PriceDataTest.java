package org.yats.trading;

import junit.framework.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created
 * Date: 11/04/15
 * Time: 16:50
 */
public class PriceDataTest
{

        @Test(groups = { "inMemory" })
        public void canCompareDepth() {
            boolean EvsE0 = TestPriceData.TEST_EURUSD.isSameBestRowsAs(TestPriceData.TEST_EURUSD,0);
            Assert.assertTrue(EvsE0);
            boolean EvsE5 = TestPriceData.TEST_EURUSD.isSameBestRowsAs(TestPriceData.TEST_EURUSD,5);
            Assert.assertTrue(EvsE5);
            boolean EvsU0 = TestPriceData.TEST_EURUSD.isSameBestRowsAs(TestPriceData.TEST_USDCHF,0);
            Assert.assertTrue(EvsU0);
            boolean EvsU5 = TestPriceData.TEST_EURUSD.isSameBestRowsAs(TestPriceData.TEST_USDCHF,5);
            Assert.assertFalse(EvsU5);
        }

        @Test(groups = { "inMemory" })
        public void canCompareBBO() {
            boolean EvsE = TestPriceData.TEST_EURUSD.isSameBestBidOfferLastAs(TestPriceData.TEST_EURUSD);
            Assert.assertTrue(EvsE);
            boolean EvsU = TestPriceData.TEST_EURUSD.isSameBestBidOfferLastAs(TestPriceData.TEST_USDCHF);
            Assert.assertFalse(EvsU);
        }


        @BeforeMethod(alwaysRun = true)
        public void setUp() {

        }

}
