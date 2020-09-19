package io.zwt.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zwt.App;
import io.zwt.domain.DataRecord;
import io.zwt.domain.model.HeartBeat;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

import static io.zwt.App.encryptedKey;
import static io.zwt.config.Config.GATEWAY;

public class LANTask extends Thread {

  private Selector selector;
  private App app;
  private StringProperty value;
  private StringProperty token;
  private StringProperty ip;
  private volatile SimpleObjectProperty<HeartBeat> heartBeat;
  private ObjectMapper objectMapper;

  public String getToken() {
    return token.get();
  }

  public StringProperty tokenProperty() {
    return token;
  }

  public void setToken(String token) {
    this.token.set(token);
  }

  public HeartBeat getHeartBeat() {
    return heartBeat.get();
  }

  public SimpleObjectProperty<HeartBeat> heartBeatProperty() {
    return heartBeat;
  }

  public void setHeartBeat(HeartBeat heartBeat) {
    this.heartBeat.set(heartBeat);
  }

  public StringProperty valueProperty() {
    return value;
  }

  public void setValue(String value) {
    this.value.set(value);
  }

  public String getIp() {
    return ip.get();
  }

  public StringProperty ipProperty() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip.set(ip);
  }

  public LANTask(Selector selector, App app) {
    this.selector = selector;
    this.app = app;
    this.objectMapper = new ObjectMapper().configure(DeserializationFeature.EAGER_DESERIALIZER_FETCH, true);
  }

  @Override
  public void run() {
    value = new SimpleStringProperty();
    token = new SimpleStringProperty();
    ip = new SimpleStringProperty();
    heartBeat = new SimpleObjectProperty<>();

    /* Paho MQTT Client*/
    String topic = "gdouzwt/feeds/light";
    String content = "Message from MqttPublishSample";
    int qos = 2;
    String broker = "tcp://io.adafruit.com:1883";
    String clientId = "9a78c480-ef23-4b0b-8977-417d9d47fd36";
    MemoryPersistence persistence = new MemoryPersistence();
    // mqtt setup
    MqttClient sampleClient = null;
    try {
      sampleClient = new MqttClient(broker, clientId, persistence);
    } catch (MqttException e) {
      e.printStackTrace();
    }
    MqttConnectOptions connOpts = new MqttConnectOptions();
    connOpts.setUserName("gdouzwt");
    connOpts.setPassword("aio_dPae44yT5NI2jmIxNLfM3LmlqnXw".toCharArray());
    //connOpts.set
    connOpts.setCleanSession(true);
    System.out.println("Connecting to broker: " + broker);
    try {
      sampleClient.connect(connOpts);
      sampleClient.subscribe("gdouzwt/feeds/light");
      sampleClient.setCallback(new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
          System.out.println("Awesome!");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
          String received = new String(message.getPayload());
          if (received.contains("write")) {
            try {
              App.channel.send(ByteBuffer.wrap(message.getPayload()), GATEWAY);
            } catch (IOException ioException) {
              ioException.printStackTrace();
            }
          }
          Platform.runLater(() -> {
            setIp(received);
          });
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
          System.out.println("Something Good");
        }
      });
    } catch (MqttException e) {
      e.printStackTrace();
    }
    System.out.println("Connected");

    while (true) {
      try {

        if (selector.select(11000) == 0) {
          System.out.println("Waiting for heartbeat sync...");
          continue;
        }
        for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext(); ) {
          SelectionKey selectionKey = iterator.next();
          if (selectionKey.isReadable()) {
            DatagramChannel selectedChannel = (DatagramChannel) selectionKey.channel();
            DataRecord dataRecord = (DataRecord) selectionKey.attachment();
            dataRecord.buffer.clear();
            dataRecord.address = selectedChannel.receive(dataRecord.buffer);
            if (dataRecord.address != null) {
              String data = app.onReceiveData(dataRecord.buffer);
              HeartBeat beat = objectMapper.readValue(data, HeartBeat.class);
              // 发送到 MQTT
              //System.out.println("Publishing message: " + content);
              /*MqttMessage message = new MqttMessage(beat.getData().toString().getBytes());
              message.setQos(qos);
              sampleClient.publish(topic, message);
              System.out.println("Message published");*/
              // System.exit(0);
              //Platform.runLater(() -> setIp(beat.getData().getContent()));
              System.out.println(beat.getData());
              if (encryptedKey != null) {
                selectionKey.interestOps(SelectionKey.OP_WRITE);
              }
            }
          }
          if (selectionKey.isValid() && selectionKey.isWritable()) {
            if (encryptedKey == null) {
              selectionKey.interestOps(SelectionKey.OP_READ);
              break;
            } else {
              selectionKey.interestOps(SelectionKey.OP_READ);
            }
          }
          iterator.remove();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
