package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssConfiguration {
    @Bean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties){
        return new AliOssUtil(aliOssProperties.getEndpoint(),
                System.getenv("OSS_ACCESS_KEY_ID"),//获取环境变量
                System.getenv("OSS_ACCESS_KEY_SECRET"),
                aliOssProperties.getBucketName());
    }
}
