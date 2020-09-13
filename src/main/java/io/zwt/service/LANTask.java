package io.zwt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zwt.App;
import io.zwt.domain.DataRecord;
import io.zwt.domain.HeartBeat;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

import static io.zwt.App.encryptedKey;

public class LANTask extends Thread {

  private Selector selector;
  private App app;
  private StringProperty value;
  private StringProperty token;
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

  public LANTask(Selector selector, App app) {
    this.selector = selector;
    this.app = app;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public void run() {
    value = new SimpleStringProperty();
    token = new SimpleStringProperty();
    heartBeat = new SimpleObjectProperty<>();
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
              if (data.contains("heartbeat")) {
                HeartBeat beat = objectMapper.readValue(data, HeartBeat.class);
                System.out.println(beat.getData());
                String s = objectMapper.writeValueAsString(beat);
                System.out.println(s);
                Platform.runLater(() -> setHeartBeat(beat));
              }
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
