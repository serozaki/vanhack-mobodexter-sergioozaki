package com.serozaki.mobodexter.raspberryagent.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.serozaki.mobodexter.raspberryagent.model.CloudConfig;
import com.serozaki.mobodexter.raspberryagent.util.PrivateKeyReader;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class GoogleIotClient implements IotClient {
    private static Logger logger = LoggerFactory.getLogger(GoogleIotClient.class);

    private String mqttBridgeHostname = "mqtt.googleapis.com";
    private int mqttBridgePort = 8883;

    private String cloudRegion = "us-central1";
    private String projectId = "demomobodexter";
    private String registryId = "MobodexterRegistry";
    private String deviceId = "mobodexter1";
    private SignatureAlgorithm signatureAlgorithm;
    private PrivateKey privateKey;

    private MqttConnectOptions connectOptions;
    private MqttClient client;
    private AtomicInteger messageId = new AtomicInteger();

    @Override
    public void configure(CloudConfig cloudConfig) {
        try {
            this.cloudRegion = cloudConfig.findStringProperty(GoogleIotClientProperties.CLOUDREGION, null);
            this.projectId = cloudConfig.findStringProperty(GoogleIotClientProperties.PROJECTID, null);
            this.registryId = cloudConfig.findStringProperty(GoogleIotClientProperties.REGISTRYID, null);
            this.deviceId = cloudConfig.getName();
            String mqttServerAddress = String.format("ssl://%s:%s", mqttBridgeHostname, mqttBridgePort);
            String mqttClientId = String.format("projects/%s/locations/%s/registries/%s/devices/%s", projectId,
                    cloudRegion, registryId, deviceId);
            configPrivateKey(cloudConfig.findStringProperty(GoogleIotClientProperties.ALGORITHM, null),
                    cloudConfig.findStringProperty(GoogleIotClientProperties.PRIVATEKEY, null));
            connectOptions = new MqttConnectOptions();
            connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            Properties sslProps = new Properties();
            sslProps.setProperty("com.ibm.ssl.protocol", "TLSv1.2");
            connectOptions.setSSLProperties(sslProps);
            connectOptions.setUserName("unused");
            client = new MqttClient(mqttServerAddress, mqttClientId, new MemoryPersistence());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void connect() throws IOException {
        try {
            connectOptions.setPassword(createJwt().toCharArray());
            client.connect(connectOptions);
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
        String topic = "/devices/" + deviceId + "/events/sensor/" + sensorId;
        MyMessage myMessage = new MyMessage(messageId.incrementAndGet(), topic, 0, payload);
        try {
            logger.info("publish {} {} {}", myMessage.getId(), myMessage.getTopic(), myMessage.getStringPayload());
            myMessage.publish(client);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void configPrivateKey(String algorithm, String privateKeyString) throws IOException {
        try {
            byte[] keyBytes = privateKeyString.getBytes(Charset.forName("UTF-8"));
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            if (algorithm.equals("RS256")) {
                privateKey = PrivateKeyReader.getPrivateKey(new ByteArrayInputStream(keyBytes), "RSA");
                signatureAlgorithm = SignatureAlgorithm.RS256;
            } else if (algorithm.equals("ES256")) {
                privateKey = KeyFactory.getInstance("EC").generatePrivate(spec);
                signatureAlgorithm = SignatureAlgorithm.ES256;
            } else {
                throw new IllegalArgumentException(
                        "Invalid algorithm " + algorithm + ". Should be one of 'RS256' or 'ES256'.");
            }
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }

    }

    private String createJwt() throws IOException {
        LocalDateTime time = LocalDateTime.now();
        // The client will be disconnected after the token expires.
        JwtBuilder jwtBuilder = Jwts.builder().setIssuedAt(Date.from(time.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(time.plusMinutes(20).atZone(ZoneId.systemDefault()).toInstant()))
                .setAudience(projectId);
        return jwtBuilder.signWith(signatureAlgorithm, privateKey).compact();
    }

    private static class MyMessage implements MqttCallback, IMqttActionListener {
        private int id;
        private String topic;
        private MqttMessage mqttMessage = new MqttMessage();

        public MyMessage(int id, String topic, int qos, String payload) {
            // mqttMessage.setId(id);
            mqttMessage.setPayload(payload.getBytes(Charset.forName("UTF-8")));
            mqttMessage.setQos(qos);
            this.topic = topic;
        }

        public int getId() {
            return id;
        }

        public String getTopic() {
            return topic;
        }

        public String getStringPayload() {
            return new String(mqttMessage.getPayload(), Charset.forName("UTF-8"));
        }

        public void publish(MqttClient client) throws MqttException {
            client.publish(topic, mqttMessage);

        }

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            logger.info("onSuccess {}", id);
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            logger.info("onFailure {}", id);
        }

        @SuppressWarnings("unused")
        public void onTimeout() {
            logger.info("onTimeout {}", id);
        }

        @Override
        public void connectionLost(Throwable e) {
            logger.info("connectionLost {}", id, e);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            logger.info("deliveryComplete {}", id);
            arg0.setActionCallback(null);

        }

        @Override
        public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
            logger.info("messageArrived {}", arg0);
        }
    }

}
