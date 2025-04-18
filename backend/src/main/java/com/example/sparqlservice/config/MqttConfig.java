package com.example.sparqlservice.config;

import java.net.URI;
import java.util.UUID;

import javax.annotation.processing.Generated;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.StringUtils;

import com.example.sparqlservice.service.MessageHandlerService;
import com.example.sparqlservice.service.RdfService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Generated(value = "com.asyncapi.generator.template.spring", date = "2024-12-12T03:04:06.957Z")
@Configuration
@RequiredArgsConstructor
public class MqttConfig {

    private final RdfService rdfService;
    private final ObjectMapper objectMapper;

    @Value("${mqtt.broker.address}")
    private String address;

    @Value("${mqtt.broker.timeout.connection}")
    private int connectionTimeout;

    @Value("${mqtt.broker.timeout.disconnection}")
    private long disconnectionTimeout;

    @Value("${mqtt.broker.timeout.completion}")
    private long completionTimeout;

    // @Value("${mqtt.broker.clientId}")
    // private String clientId;

    @Value("${mqtt.broker.username}")
    private String username;

    @Value("${mqtt.broker.password}")
    private String password;

    @Value("${mqtt.topic}")
    private String topic;

    @Value("${server.url}")
    private String serverUrl;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setServerURIs(new String[] { address });
        if (StringUtils.hasLength(username)) {
            options.setUserName(username);
        }
        if (StringUtils.hasLength(password)) {
            options.setPassword(password.toCharArray());
        }
        options.setConnectionTimeout(connectionTimeout);
        factory.setConnectionOptions(options);
        options.setAutomaticReconnect(true);

        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInbound() {
        log.debug("MQTT TOPIC = {}", topic);
        // Generate a unique clientId using the domain name and UUID.
        String domainName = URI.create(serverUrl).getHost();
        String customClientId = "client-" + domainName + "-" + UUID.randomUUID().toString();
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(customClientId,
                mqttClientFactory(), topic);
        adapter.setOutputChannel(mqttInputChannel());
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandlerService messageHandlerService() {
        return new MessageHandlerService(rdfService, objectMapper);
    }

    @Bean
    public IntegrationFlow mqttInboundFlow() {
        return IntegrationFlow.from(mqttInbound())
                .handle(messageHandlerService())
                .get();
    }

    /*
     * This section below is commented out until the MQTT publishing feature needs
     * to be
     * implemented.
     */

    // @Bean
    // public MessageProducerSupport Inbound() {
    // MqttPahoMessageDrivenChannelAdapter adapter = new
    // MqttPahoMessageDrivenChannelAdapter(clientId,
    // mqttClientFactory(), topic);
    // adapter.setCompletionTimeout(connectionTimeout);
    // adapter.setDisconnectCompletionTimeout(disconnectionTimeout);
    // adapter.setConverter(new DefaultPahoMessageConverter());
    // return adapter;
    // }

    // @Bean
    // @ServiceActivator(outputChannel = "OutboundChannel")
    // public MessageHandler Outbound() {
    // MqttPahoMessageHandler pahoMessageHandler = new
    // MqttPahoMessageHandler(clientId, mqttClientFactory());
    // pahoMessageHandler.setAsync(true);
    // pahoMessageHandler.setCompletionTimeout(completionTimeout);
    // pahoMessageHandler.setDisconnectCompletionTimeout(disconnectionTimeout);
    // pahoMessageHandler.setDefaultTopic(Topic);

    // return pahoMessageHandler;
    // }

}
