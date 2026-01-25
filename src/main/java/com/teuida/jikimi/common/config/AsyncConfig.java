package com.teuida.jikimi.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring 기본 TaskExecutor 사용
}
