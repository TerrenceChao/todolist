package com.example.todolist.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

@Slf4j
@Configuration
public class GoogleCloudConfig {

    @Autowired
    private Environment env;

    @Bean(name = "googleCloudStorage")
    public Storage getCloudStorage() throws IOException {
        String key = Objects.requireNonNull(env.getProperty("google.cloud.storage.key"));
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(key))
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

        return storage;
    }
}
