package com._anhtai.aistudymentor.config;

import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public ChatClient.Builder chatClientBuilder() {
        ChatClient.Builder builder = Mockito.mock(ChatClient.Builder.class);
        ChatClient chatClient = Mockito.mock(ChatClient.class);
        Mockito.when(builder.build()).thenReturn(chatClient);
        return builder;
    }
}
