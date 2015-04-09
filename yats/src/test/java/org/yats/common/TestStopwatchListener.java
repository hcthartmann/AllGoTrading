package org.yats.common;

import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * Created
 * Date: 09/04/15
 * Time: 10:27
 */
public class TestStopwatchListener extends TestListenerAdapter {

    final Logger log = LoggerFactory.getLogger(TestStopwatchListener.class);

    @Override
    public void onTestStart(ITestResult tr) {
        watch.reset();
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
       logTimeTaken();
    }

    @Override
    public void onTestFailure(ITestResult tr) {
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
    }

    public void logTimeTaken() {
        Duration timeTaken = watch.getElapsedTime();
        String msg = "timeTaken=" + timeTaken.getMillis()+"ms";
        log.debug(msg);
    }

    public TestStopwatchListener() {
        watch = new Stopwatch();
    }

    /////////////////////////////////////////////////////////////////

    private Stopwatch watch;

}
