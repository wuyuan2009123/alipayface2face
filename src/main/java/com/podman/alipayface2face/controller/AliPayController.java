package com.podman.alipayface2face.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.ijpay.alipay.AliPayApi;
import com.ijpay.alipay.AliPayApiConfig;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.alipay.api.AlipayConstants.NOTIFY_URL;
import static com.podman.alipayface2face.util.StringUtil.generateQrCodeBase64;
import static com.podman.alipayface2face.util.StringUtil.getOutTradeNo;
import static com.podman.alipayface2face.util.TradeStatusUtil.getStatusDesc;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AliPayController {

    private final AliPayApiConfig aliPayApiConfig;

    @Value("${alipay_notify_url}")
    private String alipayNotifyUrl;

    @GetMapping(value = "/create_order")
    public Map<String,Object> tradePreCreatePay(String amount,String subject,String timeout_express) {
        String notifyUrl = alipayNotifyUrl + "/" + NOTIFY_URL;
        String order_id = getOutTradeNo();
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setSubject(subject);
        model.setTotalAmount(amount);
        model.setTimeoutExpress(timeout_express);
        model.setOutTradeNo(order_id);
        try {
            String resultStr = AliPayApi.tradePrecreatePayToResponse(model, notifyUrl).getBody();
            JSONObject jsonObject = JSONObject.parseObject(resultStr);
            String qr_code_ali = jsonObject.getJSONObject("alipay_trade_precreate_response").getString("qr_code");
            String qrCodeBase64 = generateQrCodeBase64(qr_code_ali);
            return Map.of("code",0 ,"qr_code",qrCodeBase64,"order_id",order_id,"amount",amount);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Map.of();
    }

    @PostMapping(value = "/notify_url")
    public String notifyUrl(HttpServletRequest request) {
        log.info("notify....init");
        try {
            // 获取支付宝POST过来反馈信息
            Map<String, String> params = AliPayApi.toMap(request);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
            boolean verifyResult = AlipaySignature.rsaCheckV1(params, aliPayApiConfig.getAliPayPublicKey(), "UTF-8", "RSA2");
            if (verifyResult) {

                System.out.println("notify_url 验证成功succcess");
                return "success";
            } else {
                System.out.println("notify_url 验证失败");
                return "failure";
            }
        } catch (AlipayApiException e) {
            log.error(e.getMessage());
            return "failure";
        }
    }

    /**
     * 交易查询
     */
    @RequestMapping(value = "/tradeQuery/{orderId}")
    public Map<String,Object> tradeQuery(@PathVariable String orderId) {
        log.info("tradeQuery oderId:{}",orderId);
        try {
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            if (StringUtils.isNotEmpty(orderId)) {
                model.setOutTradeNo(orderId);
            }
            String resultStr = AliPayApi.tradeQueryToResponse(model).getBody();
            log.info("tradeQuery body:{}",resultStr);
            JSONObject jsonObject = JSONObject.parseObject(resultStr);
            JSONObject alipayTradeQueryResponse = jsonObject.getJSONObject("alipay_trade_query_response");
            if (!"TRADE_SUCCESS".equals(alipayTradeQueryResponse.getString("trade_status"))) {
                return Map.of("code", 1, "msg", alipayTradeQueryResponse.getString("msg"));
            }
            return Map.of("code", 0,
                    "msg", "success",
                    "trade_no", alipayTradeQueryResponse.getString("trade_no"),
                    "status",alipayTradeQueryResponse.getString("trade_status"),
                    "status_desc",getStatusDesc(alipayTradeQueryResponse.getString("trade_status")),
                    "order_id",alipayTradeQueryResponse.getString("out_trade_no"),
                    "amount",alipayTradeQueryResponse.getString("total_amount"),
                    "logon_id",alipayTradeQueryResponse.getString("buyer_logon_id"),
                    "user_id",alipayTradeQueryResponse.getString("buyer_open_id"));
        } catch (AlipayApiException e) {
            log.error(e.getMessage());
        }
        return Map.of();
    }


}
