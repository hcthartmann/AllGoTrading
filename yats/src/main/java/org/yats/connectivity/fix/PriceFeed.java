package org.yats.connectivity.fix;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

import org.yats.trading.IConsumeMarketData;
import org.yats.trading.IProvidePriceFeed;
import org.yats.trading.Product;
import quickfix.*;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.NoMDEntryTypes;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityID;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;

public class PriceFeed implements IProvidePriceFeed {

    public synchronized void logon()
    {
        if (!initiatorStarted) {
            try {
                initiator.start();
            } catch (ConfigError configError) {
                configError.printStackTrace();
                throw new RuntimeException(configError.getMessage());
            }
            initiatorStarted = true;
         } else {
            for (SessionID sessionId : initiator.getSessions()) {
                Session.lookupSession(sessionId).logon();
            }
        }
    }

    @Override
    public void subscribe(Product product, IConsumeMarketData consumer)
    {
        Random generator = new Random();
        int r = generator.nextInt();
        quickfix.fix42.MarketDataRequest marketDataRequest = new quickfix.fix42.MarketDataRequest(
                new MDReqID(Integer.toString(r * r)),
                new SubscriptionRequestType('1'), new MarketDepth(1));

        quickfix.fix42.MarketDataRequest.NoRelatedSym group = new quickfix.fix42.MarketDataRequest.NoRelatedSym();

        SessionID sessionId = initiator.getSessions().get(0);

        group.set(new Symbol(product.getSymbol()));
        group.set(new SecurityID(product.getId()));
        group.set(new SecurityExchange(product.getExchange()));

        marketDataRequest.setField(new MDUpdateType(0));
        marketDataRequest.setField(new NoMDEntryTypes(0));
        marketDataRequest.addGroup(group);

        try {
            application.setMarketDataConsumer(consumer);
            Session.sendToTarget(marketDataRequest, sessionId);
        } catch (SessionNotFound e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }



    public static PriceFeed create()
    {
        String configStringDefault = "[default]\n" +
                "FileStorePath=data\n" +
                "ConnectionType=initiator\n" +
                "SenderCompID=HIQ1_PRICE\n" +
                "TargetCompID=HIQFIX\n" +
                "SocketConnectHost=46.244.8.46\n" +
                "StartTime=00:00:00\n" +
                "EndTime=00:00:00\n" +
                "HeartBtInt=30\n" +
                "ReconnectInterval=5\n" +
                "\n" +
                "[session]\n" +
                "BeginString=FIX.4.2\n" +
                "SocketConnectPort=5001";

        return createFromConfigString(configStringDefault);
    }

    public static PriceFeed createFromConfigFile(String pathToConfigFile)
    {
        try {
            String configAsString = new Scanner(new File(pathToConfigFile)).useDelimiter("\\Z").next();
            return createFromConfigString(configAsString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private static PriceFeed createFromConfigString(String config)
    {
        try {
            InputStream inputStream = new ByteArrayInputStream(config.getBytes());
            SessionSettings settings = new SessionSettings(inputStream);
            inputStream.close();
            return new PriceFeed(settings);
        } catch (ConfigError configError) {
            configError.printStackTrace();
            throw new RuntimeException(configError.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public PriceFeed(SessionSettings settings) throws Exception {

        boolean logHeartbeats = Boolean.valueOf(System.getProperty("logHeartbeats", "true"));

        application = new PriceFeedCracker();

        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true, true, true, logHeartbeats);
        MessageFactory messageFactory = new DefaultMessageFactory();

        initiator = new SocketInitiator(application, messageStoreFactory,
                settings, logFactory, messageFactory);

    }

    private boolean initiatorStarted = false;
    private static Initiator initiator = null;
    private PriceFeedCracker application;

}