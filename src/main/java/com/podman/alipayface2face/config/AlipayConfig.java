package com.podman.alipayface2face.config;

import com.ijpay.alipay.AliPayApiConfig;
import com.ijpay.alipay.AliPayApiConfigKit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AlipayConfig {


    @Value("${alipay_app_id}")
    private String appId;

    @Value("${alipay_private}")
    private String privateKey;

    @Value("${alipay_public}")
    private String publicKey;

    @Bean
    public AliPayApiConfig aliPayApiConfig(){

        AliPayApiConfig aliPayApiConfig = AliPayApiConfig.builder()
                .setAppId(appId)
                .setAliPayPublicKey(publicKey)
                .setCharset("UTF-8")
                .setPrivateKey(privateKey)
                .setServiceUrl("https://openapi.alipay.com/gateway.do")
                .setSignType("RSA2")
                // 普通公钥方式
                .build();

        AliPayApiConfigKit.setThreadLocalAppId(appId);
        AliPayApiConfigKit.setThreadLocalAliPayApiConfig(aliPayApiConfig);
        log.info("appId:{}",aliPayApiConfig.getAppId());
        // 普通公钥方式
        return aliPayApiConfig;
    }

}
