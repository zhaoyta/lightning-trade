package com.lightningtrade.easyquant.execution;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;
import com.tigerbrokers.stock.openapi.client.https.request.TigerHttpRequest;
import com.tigerbrokers.stock.openapi.client.https.response.TigerHttpResponse;
import com.tigerbrokers.stock.openapi.client.struct.enums.ActionType;
import com.tigerbrokers.stock.openapi.client.struct.enums.Currency;
import com.tigerbrokers.stock.openapi.client.struct.enums.MethodName;
import com.tigerbrokers.stock.openapi.client.struct.enums.OrderType;
import com.tigerbrokers.stock.openapi.client.struct.enums.SecType;
import com.tigerbrokers.stock.openapi.client.util.builder.TradeParamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TradeExecutor {
    private static final Logger logger = LoggerFactory.getLogger(TradeExecutor.class);

    @Autowired
    private TigerHttpClient client;

    /**
     * 市价单下单
     * 
     * @param symbol   股票代码
     * @param quantity 数量
     * @param action   买卖方向 (BUY/SELL)
     * @return 订单ID，如果下单失败返回null
     */
    public Long placeMarketOrder(String symbol, int quantity, SecType secType, Currency currency, ActionType action) {
        try {
            logger.info("下市价单 - 股票: {}, 数量: {}, 方向: {}", symbol, quantity, action);

            TigerHttpRequest request = new TigerHttpRequest(MethodName.PLACE_ORDER);

            String bizContent = TradeParamBuilder.instance()
                    .symbol(symbol)
                    .secType(secType)
                    .currency(currency)
                    .action(action)
                    .orderType(OrderType.MKT)
                    .totalQuantity(quantity)
                    .buildJson();

            request.setBizContent(bizContent);
            TigerHttpResponse response = client.execute(request);

            if (response.isSuccess()) {
                JSONObject data = JSON.parseObject(response.getData());
                Long orderId = data.getLong("id");
                logger.info("市价单下单成功 - 订单ID: {}", orderId);
                return orderId;
            } else {
                logger.error("市价单下单失败 - 错误码: {}, 错误信息: {}",
                        response.getCode(), response.getMessage());
                return null;
            }
        } catch (Exception e) {
            logger.error("市价单下单异常", e);
            return null;
        }
    }

    /**
     * 限价单下单
     * 
     * @param symbol   股票代码
     * @param quantity 数量
     * @param price    价格
     * @param action   买卖方向 (BUY/SELL)
     * @return 订单ID，如果下单失败返回null
     */
    public Long placeLimitOrder(String symbol, int quantity, double price,
            SecType secType, Currency currency, ActionType action) {
        try {
            logger.info("下限价单 - 股票: {}, 数量: {}, 价格: {}, 方向: {}",
                    symbol, quantity, price, action);

            TigerHttpRequest request = new TigerHttpRequest(MethodName.PLACE_ORDER);

            String bizContent = TradeParamBuilder.instance()
                    .symbol(symbol)
                    .secType(secType)
                    .currency(currency)
                    .action(action)
                    .orderType(OrderType.LMT)
                    .totalQuantity(quantity)
                    .limitPrice(price)
                    .buildJson();

            request.setBizContent(bizContent);
            TigerHttpResponse response = client.execute(request);

            if (response.isSuccess()) {
                JSONObject data = JSON.parseObject(response.getData());
                Long orderId = data.getLong("id");
                logger.info("限价单下单成功 - 订单ID: {}", orderId);
                return orderId;
            } else {
                logger.error("限价单下单失败 - 错误码: {}, 错误信息: {}",
                        response.getCode(), response.getMessage());
                return null;
            }
        } catch (Exception e) {
            logger.error("限价单下单异常", e);
            return null;
        }
    }

    /**
     * 取消订单
     */
    public boolean cancelOrder(Long orderId) {
        try {
            logger.info("取消订单 - 订单ID: {}", orderId);

            // 创建取消请求
            TigerHttpRequest request = new TigerHttpRequest(MethodName.CANCEL_ORDER);

            String bizContent = TradeParamBuilder.instance()
                    .id(orderId)
                    .buildJson();

            request.setBizContent(bizContent);
            TigerHttpResponse response = client.execute(request);
            JSONObject data = JSON.parseObject(response.getData());

            // 检查响应
            if (response.isSuccess()) {
                Long id = data.getLong("id");
                logger.info("订单取消成功 - 订单ID: {}", id);
                return true;
            } else {
                logger.error("订单取消失败 - 订单ID: {}, 错误码: {}, 错误信息: {}",
                        orderId, response.getCode(), response.getMessage());
                return false;
            }
        } catch (Exception e) {
            logger.error("订单取消异常", e);
            return false;
        }
    }
}