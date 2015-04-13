package org.yats.connectivity.xchange;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.bitfinex.v1.BitfinexExchange;
import com.xeiam.xchange.bitfinex.v1.BitfinexOrderType;
import com.xeiam.xchange.bitfinex.v1.dto.account.BitfinexBalancesResponse;
import com.xeiam.xchange.bitfinex.v1.dto.trade.BitfinexOrderStatusResponse;
import com.xeiam.xchange.bitfinex.v1.service.polling.BitfinexAccountServiceRaw;
import com.xeiam.xchange.bitfinex.v1.service.polling.BitfinexTradeServiceRaw;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.trade.LimitOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.Mapping;
import org.yats.common.PropertiesReader;
import org.yats.common.UniqueId;
import org.yats.connectivity.ConnectivityExceptions;
import org.yats.trading.*;

import java.io.IOException;

/**
 * Created
 * Date: 12/04/15
 * Time: 00:03
 */

public class BitfinexTrading implements IProvideTrading
{

    @Override
    public boolean login() {
        Mapping<String, Decimal> map = getAssets();
        return map.containsKey("trading_usd");
    }

    @Override
    public Receipt getReceipt(UniqueId orderId) {
        String xId = mapOid2Xid.get(orderId.toString());
        return mapXid2Receipt.get(xId);
    }

    @Override
    public Mapping<String, Decimal> getAssets()
    {
        Mapping<String, Decimal> map = new Mapping<String, Decimal>();
        try {
            BitfinexBalancesResponse[] responseArray = accountService.getBitfinexAccountInfo();
            for(BitfinexBalancesResponse r : responseArray) {
                map.put(r.getType()+"_"+r.getCurrency(), new Decimal(r.getAmount()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectivityExceptions.ConnectionException(e.getMessage());
        }
        return map;
    }

    @Override
    public void cancelAllOrders() {
        try {
            BitfinexOrderStatusResponse[] responseArray = tradeService.getBitfinexOpenOrders();
            for(BitfinexOrderStatusResponse response : responseArray)
            {
                tradeService.cancelBitfinexOrder(""+response.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectivityExceptions.ConnectionException(e.getMessage());
        }
    }

    @Override
    public Mapping<String, Receipt> getOpenOrderMap() {
        Mapping<String, Receipt> receiptMap = new Mapping<String, Receipt>();
        for(Receipt r : mapXid2Receipt.values()) {
            if(r.isEndState()) continue;
            receiptMap.put(r.getOrderIdString(), r);
        }
        return receiptMap;
    }

    @Override
    public void updateReceipts() {
        try {
            BitfinexOrderStatusResponse[] responseArray = tradeService.getBitfinexOpenOrders();
            for(BitfinexOrderStatusResponse response : responseArray)
            {
                String xId = ""+response.getId();
                if(!mapXid2Receipt.containsKey(xId)) continue; // ignore unknown orders for now, probably manual
                Receipt receipt = mapXid2Receipt.get(xId);
                updateReceiptFromResponse(receipt, response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectivityExceptions.ConnectionException(e.getMessage());
        }
    }

    @Override
    public void sendOrderNew(OrderNew newOrder)
    {

        Order.OrderType side = newOrder.getBookSide()== BookSide.BID ? Order.OrderType.BID:Order.OrderType.ASK;
        CurrencyPair pair = mapPid2Bfxid.get(newOrder.getProductId());
        LimitOrder limitOrder = new LimitOrder
                .Builder(side, pair)
                .limitPrice(newOrder.getLimit().toBigDecimal())
                .tradableAmount(newOrder.getSize().toBigDecimal())
                .build();

        try {
            BitfinexOrderStatusResponse response = tradeService.placeBitfinexLimitOrder(limitOrder, BitfinexOrderType.MARGIN_LIMIT, false);
            Receipt receipt = newOrder.createReceiptDefault()
                    .withExternalAccount("margin");
            updateReceiptFromResponse(receipt, response);
            String xId = ""+response.getId();
            mapXid2Receipt.put(xId, receipt);
            mapOid2Xid.put(receipt.getOrderIdString(), xId);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectivityExceptions.ConnectionException(e.getMessage());
        }
    }

    @Override
    public void sendOrderCancel(OrderCancel o) {
        String xId = mapOid2Xid.get(o.getOrderIdString());
        try {
            boolean canceled = tradeService.cancelBitfinexOrder(xId);
            if(!canceled)
                throw new ConnectivityExceptions.UnexpectedExternalInputException("Cannot cancel "+xId);
            BitfinexOrderStatusResponse response;
            do {
                log.debug("getting order status");
                response = tradeService.getBitfinexOrderStatus(xId);
            } while(!response.isCancelled() || response.isLive());
            Receipt receipt = mapXid2Receipt.get(xId);
            updateReceiptFromResponse(receipt, response);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectivityExceptions.ConnectionException(e.getMessage());
        }
    }

    public static class Factory
    {
        public BitfinexTrading createFromProperties(PropertiesReader prop) {
            Exchange exchange = ExchangeFactory.INSTANCE.createExchange(BitfinexExchange.class.getName());
            ExchangeSpecification bfxSpec = exchange.getDefaultExchangeSpecification();
            bfxSpec.setApiKey(prop.get("apikey"));
            bfxSpec.setSecretKey(prop.get("apisecret"));
            exchange.applySpecification(bfxSpec);
            BitfinexTradeServiceRaw tradeService = (BitfinexTradeServiceRaw) exchange.getPollingTradeService();
            BitfinexAccountServiceRaw accountService = (BitfinexAccountServiceRaw) exchange.getPollingAccountService();
            return new BitfinexTrading(tradeService, accountService);
        }
    }

    public BitfinexTrading(
            BitfinexTradeServiceRaw _tradeService,
            BitfinexAccountServiceRaw _accountService)
    {
        tradeService = _tradeService;
        accountService = _accountService;
        mapXid2Receipt = new Mapping<String, Receipt>();
        mapOid2Xid = new Mapping<String, String>();
        mapPid2Bfxid = new Mapping<String, CurrencyPair>();
        mapPid2Bfxid.put("BFX_XBTUSD", CurrencyPair.BTC_USD);
        mapPid2Bfxid.put("BFX_LTCUSD", CurrencyPair.LTC_USD);
        mapPid2Bfxid.put("BFX_LTCBTC", CurrencyPair.LTC_BTC);
    }

    /////////////////////////////////////////////////////////////////////////////////

    private void updateReceiptFromResponse(Receipt receipt, BitfinexOrderStatusResponse response) {
        receipt.setEndState(response.isCancelled() || !response.isLive());
        receipt.setTotalTradedSize(new Decimal(response.getOriginalAmount().subtract(response.getRemainingAmount())));
        receipt.setCurrentTradedSize(new Decimal(response.getExecutedAmount()));
        receipt.setResidualSize(new Decimal(response.getRemainingAmount()));
    }

    private BitfinexTradeServiceRaw tradeService;
    private BitfinexAccountServiceRaw accountService;
    private Mapping<String, Receipt> mapXid2Receipt;
    private Mapping<String, String> mapOid2Xid;
    private Mapping<String, CurrencyPair> mapPid2Bfxid;

    final private Logger log = LoggerFactory.getLogger(BitfinexTrading.class);
}
