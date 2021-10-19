package com.example.todolist.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class RedissonConfig {

    @Autowired
    private Environment env;

    @Bean
    public RedissonClient config() {
        Config config = new Config();
        // NIO 模式
        config.setTransportMode(TransportMode.NIO);

        // 集群模式
        // config.useClusterServers().addNodeAddress(env.getProperty("redisson.host.config"), env.getProperty("redisson.host.config"));

        // 目前為 “單一節點模式"
        config.useSingleServer()
                .setAddress(env.getProperty("redisson.host.config"))
                .setKeepAlive(true);

        return Redisson.create(config);
    }
}
