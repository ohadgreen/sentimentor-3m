package com.acme.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AnalysisRequestConsumerConfig {

    @Value("${consumer.pool.core-pool-size}")
    private int corePoolSize;

    @Value("${consumer.pool.max-pool-size}")
    private int maxPoolSize;

    @Bean(name = "sentimentConsumerExecutor")
    public Executor sentimentConsumerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("SentimentConsumer-");
        executor.initialize();
        return executor;
    }
}
