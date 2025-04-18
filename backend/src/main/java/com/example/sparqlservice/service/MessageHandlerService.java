package com.example.sparqlservice.service;

import org.springframework.messaging.Message;

import com.example.sparqlservice.constants.AirwayNotificationStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.lang.NonNull;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import javax.annotation.processing.Generated;

@Generated(value = "com.asyncapi.generator.template.spring", date = "2024-12-12T03:04:06.886Z")
@Slf4j
public class MessageHandlerService {

    private final RdfService rdfService;
    private final ObjectMapper objectMapper;

    public MessageHandlerService(RdfService rdfService, ObjectMapper objectMapper) {
        this.rdfService = rdfService;
        this.objectMapper = objectMapper;
    }

    /**
     * Handles incoming messages from the MQTT channel.
     *
     * @param message
     */
    public void handle(Message<?> message) {
        if (message == null || message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC) == null) {
            log.warn("Received a message with null headers or topic.");
            return;
        }

        final String topic = extractTopic(message);
        final AirwayNotificationStatus status = extractStatus(message);
        log.info("MQTT message receive ...\nTopic : {}\nStatus : {}", topic, status);

        String airwayId = extractAirwayId(topic);
        log.debug("Extracted Airway ID: {}", airwayId);

        if (airwayId.isEmpty() || status == null) {
            log.warn("Received message with empty Airway ID or null status.");
            return;
        }
        rdfService.handleAirwayNotification(airwayId, status);
    }

    private String extractTopic(@NonNull Message<?> message) {
        Object topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        if (topic == null) {
            log.error("Received message with null topic.");
            return "";
        }
        return topic.toString();
    }

    private String extractAirwayId(String topic) {
        String[] topicParts = topic.split("/");
        if (topicParts.length != 4) {
            log.error("Received message with unexpected topic format. Expected 4 parts but got {}", topicParts.length);
            return "";
        }
        return topicParts[3];
    }

    /**
     * Extracts the status from the message payload.
     *
     * @param message the message containing the payload
     * @return the extracted AirwayNotificationStatus, or null if the status is
     *         invalid or cannot be extracted
     */
    private AirwayNotificationStatus extractStatus(Message<?> message) {
        final String AIRWAY_KEY = "airway";
        String payload = (String) message.getPayload();
        log.debug("Message payload string: {}", payload);
        String statusString = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> messageMap = objectMapper.readValue(payload, Map.class);
            if (!messageMap.containsKey(AIRWAY_KEY)) {
                throw new IllegalArgumentException("Status key is missing in the message payload.");
            }

            Object airwayObject = messageMap.get(AIRWAY_KEY);

            if (airwayObject != null) {
                return AirwayNotificationStatus.CREATE_OR_UPDATE;
            } else {
                return AirwayNotificationStatus.DELETE;
            }

        } catch (JsonProcessingException e) {
            log.error("Error processing JSON message: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status value received: {}", statusString);
            return null;
        }
    }
}
