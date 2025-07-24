package com.podman.alipayface2face.util;

import java.util.HashMap;
import java.util.Map;

public class TradeStatusUtil {
    private static final Map<String, String> STATUS_DESC = new HashMap<>();

    static {
        STATUS_DESC.put("WAIT_BUYER_PAY", "等待付款");
        STATUS_DESC.put("TRADE_CLOSED", "交易关闭");
        STATUS_DESC.put("TRADE_SUCCESS", "支付成功");
        STATUS_DESC.put("TRADE_FINISHED", "交易完成");
    }

    public static String getStatusDesc(String tradeStatus) {
        return STATUS_DESC.getOrDefault(tradeStatus, "未知状态");
    }
}
