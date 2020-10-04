package io.zwt.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zwt.App;
import io.zwt.controller.HomeController;
import io.zwt.domain.DataRecord;
import io.zwt.domain.model.cmd.Data;
import io.zwt.domain.model.data.HeartBeat;
import io.zwt.domain.model.data.Other;
import io.zwt.domain.model.data.PlugReportData;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

import static io.zwt.App.encryptedKey;

/**
 * 后台线程，负责处理局域网相关的操作
 */
public class LANTask extends Thread {

  private final Selector selector;
  private final App app;
  private StringProperty value;
  private StringProperty token;
  private StringProperty ip;
  private volatile SimpleObjectProperty<HeartBeat> heartBeat;
  private final ObjectMapper objectMapper;
  static final Logger log = LoggerFactory.getLogger(LANTask.class);

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

    while (true) {
      try {

        if (selector.select(11000) == 0) {
          log.debug("Waiting for heartbeat sync...");
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

              // 如果是网关的心跳
              if (data.contains("heartbeat") && data.contains("gateway")) {
                HeartBeat beat = objectMapper.readValue(data, HeartBeat.class);
                log.debug(beat.getData());
              } else {
                if (data.contains("iam")) {
                  log.debug(data);
                } else {
                  Other other = objectMapper.readValue(data, Other.class);
                  log.debug(other.getData());
                  if (other.getCmd().equals("get_id_list_ack")) {  // 获取网关子设备
                    String[] strings = objectMapper.readValue(other.getData(), String[].class);
                    System.out.println("网关子设备 id: ");
                    for (String string : strings) {
                      System.out.printf("%s\t", string);
                    }
                    System.out.println();
                  }
                  if ((other.getCmd().equals("report") || other.getCmd().equals("read_ack"))) {
                    if (other.getModel().equals("gateway")) {
                      Data lampDataReport = objectMapper.readValue(other.getData(), Data.class);
                      HomeController.lampSelected.setValue((lampDataReport.rgbProperty().getValue() & 0xff << 24) > 0);
                    }
                    if (other.getModel().equals("plug")) {
                      PlugReportData plugReportData = objectMapper.readValue(other.getData(), PlugReportData.class);
                      HomeController.plugSelected.setValue(plugReportData.statusProperty().getValue());
                    }
                  }
                }
              }
              if (encryptedKey != null) {
                selectionKey.interestOps(SelectionKey.OP_WRITE);
              }
            }
          }
          if (selectionKey.isValid() && selectionKey.isWritable()) {
            if (encryptedKey == null) {
              selectionKey.interestOps(SelectionKey.OP_READ);
              //break;
              continue;
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
