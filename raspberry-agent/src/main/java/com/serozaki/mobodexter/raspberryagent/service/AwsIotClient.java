package com.serozaki.mobodexter.raspberryagent.service;

import java.io.IOException;
import java.security.KeyStore;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.serozaki.mobodexter.raspberryagent.model.CloudConfig;
import com.serozaki.mobodexter.raspberryagent.util.KeystoreUtil;

@Service
public class AwsIotClient implements IotClient {

    private static Logger logger = LoggerFactory.getLogger(AwsIotClient.class);
    private String clientId = "mobodexter2";

    private MyClient client;
    private AtomicInteger messageId = new AtomicInteger();

    @Override
    public void configure(CloudConfig cloudConfig) {
        logger.info("configure {} {}", cloudConfig,
                cloudConfig.findStringProperty(AwsIotClientProperties.ENDPOINT, null));
        try {
            KeystoreUtil keystoreUtil = new KeystoreUtil(
                    cloudConfig.findStringProperty(AwsIotClientProperties.CERTIFICATE, null),
                    cloudConfig.findStringProperty(AwsIotClientProperties.PRIVATEKEY, null));
            client = new MyClient(cloudConfig.findStringProperty(AwsIotClientProperties.ENDPOINT, null),
                    cloudConfig.getName(), keystoreUtil.getKeyStore(), keystoreUtil.getKeyPassword());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void connect() throws IOException {
        try {
            client.connect();
            subscribe();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() throws IOException {
        try {
            client.disconnect();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void publish(int sensorId, String payload) throws IOException {
        String topic = "devices/" + clientId + "/sensors" + sensorId;
        MyMessage myMessage = new MyMessage(messageId.incrementAndGet(), topic, AWSIotQos.QOS0, payload);
        try {
            logger.info("publish {} {} {}", myMessage.getId(), myMessage.getTopic(), myMessage.getStringPayload());
            client.publish(myMessage);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void subscribe() {
        try {
            String topic = "devices/+/sensors/+";
            MyTopic myTopic = new MyTopic(topic, AWSIotQos.QOS0);
            client.subscribe(myTopic, false);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static class MyMessage extends AWSIotMessage {
        private int id;

        public MyMessage(int id, String topic, AWSIotQos qos, String payload) {
            super(topic, qos, payload);
            this.id = id;
        }

        public int getId() {
            return id;
        }

        @Override
        public void onSuccess() {
            logger.info("onSuccess {}", id);
        }

        @Override
        public void onFailure() {
            logger.info("onFailure {}", id);
        }

        @Override
        public void onTimeout() {
            logger.info("onTimeout {}", id);
        }
    }

    private static class MyTopic extends AWSIotTopic {
        public MyTopic(String topic, AWSIotQos qos) {
            super(topic, qos);
        }

        @Override
        public void onMessage(AWSIotMessage message) {
            logger.info("onMessage topic={} payload={}", message.getTopic(), message.getStringPayload());
        }
    }

    private static class MyClient extends AWSIotMqttClient {

        public MyClient(String clientEndpoint, String clientId, KeyStore keyStore, String keyPassword) {
            super(clientEndpoint, clientId, keyStore, keyPassword);
        }

        @Override
        public void onConnectionClosed() {
            try {
                logger.info("onConnectionClosed {}", getConnectionStatus());
            } finally {
                super.onConnectionClosed();
            }
        }

        @Override
        public void onConnectionFailure() {
            try {
                logger.info("onConnectionFailure {}", getConnectionStatus());
            } finally {
                super.onConnectionFailure();
            }
        }

        @Override
        public void onConnectionSuccess() {
            try {
                logger.info("onConnectionSuccess {}", getConnectionStatus());
            } finally {
                super.onConnectionFailure();
            }
        }
    }
}
