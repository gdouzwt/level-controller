package io.zwt.service;

import io.zwt.App;
import io.zwt.domain.DataRecord;
import javafx.application.Platform;
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

  public String getValue() {
    return value.get();
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
  }

  @Override
  public void run() {
    value = new SimpleStringProperty();
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
              Platform.runLater(() -> value.setValue(data));
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
