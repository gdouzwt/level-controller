package io.zwt.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zwt.App;
import io.zwt.controller.HomeController;
import io.zwt.domain.DataRecord;
import io.zwt.domain.model.cmd.Data;
import io.zwt.domain.model.cmd.HeartBeatCmd;
import io.zwt.domain.model.cmd.IAmCmd;
import io.zwt.domain.model.data.GenericData;
import io.zwt.domain.model.data.PlugReportData;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

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
  private volatile SimpleObjectProperty<HeartBeatCmd> heartBeat;
  private final ObjectMapper objectMapper;
  static final Logger log = LoggerFactory.getLogger(LANTask.class);
  static List<Double> doubles = new ArrayList<>();

  public String getToken() {
    return token.get();
  }

  public StringProperty tokenProperty() {
    return token;
  }

  public void setToken(String token) {
    this.token.set(token);
  }

  public HeartBeatCmd getHeartBeat() {
    return heartBeat.get();
  }

  public SimpleObjectProperty<HeartBeatCmd> heartBeatProperty() {
    return heartBeat;
  }

  public void setHeartBeat(HeartBeatCmd heartBeatCmd) {
    this.heartBeat.set(heartBeatCmd);
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

        /*if (selector.select(110000) == 0) {
          log.debug("Waiting for heartbeat sync...");
          continue;
        }*/
        selector.select();
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
                HeartBeatCmd beat = objectMapper.readValue(data, HeartBeatCmd.class);
                //log.debug(beat.getData());
              } else {
                if (data.contains("iam")) {
                  IAmCmd iAmCmd = objectMapper.readValue(data, IAmCmd.class);
                } else if (data.contains("{")) {
                  GenericData genericData = objectMapper.readValue(data, GenericData.class);
                  log.debug(genericData.getData());
                  if (genericData.getCmd().equals("get_id_list_ack")) {  // 获取网关子设备
                    String[] strings = objectMapper.readValue(genericData.getData(), String[].class);
                    System.out.println("网关子设备 id: ");
                    for (String string : strings) {
                      System.out.printf("%s\t", string);
                    }
                    System.out.println();
                  }
                  if ((genericData.getCmd().equals("report") || genericData.getCmd().equals("read_ack"))) {


                    if (genericData.getModel().equals("ultrasonic")) {
                      double level = Double.parseDouble(genericData.getData());
                      doubles.add(level);
                      if (doubles.size() > 10) {
                        Stream<Double> doubleStream = doubles.stream();
                        double v = doubleStream.mapToDouble(d -> d).average().orElse(Double.NaN);
                        if (null != HomeController.level) {
                          HomeController.level.setValue(((108 - v) / 100));
                        }
                        log.debug("v " + v);
                        log.debug("doubles " + doubles);
                        doubles.clear();
                      }
                    }

                    if (genericData.getModel().equals("gateway")) {
                      Data lampDataReport = objectMapper.readValue(genericData.getData(), Data.class);
                      HomeController.lampSelected.setValue((lampDataReport.rgbProperty().getValue() & 0xff << 24) > 0);
                    }
                    if (genericData.getModel().equals("plug")) {
                      PlugReportData plugReportData = objectMapper.readValue(genericData.getData(), PlugReportData.class);
                      HomeController.plugSelected.setValue(plugReportData.statusProperty().getValue());
                    }
                  }
                }
              }
              //System.out.println(data);
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
