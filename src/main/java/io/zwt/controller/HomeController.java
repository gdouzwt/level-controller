package io.zwt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXToggleButton;
import io.zwt.App;
import io.zwt.domain.model.cmd.Whois;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * 主面板的控制器，界面交互逻辑写在这里。
 */
public class HomeController implements Initializable {

  @FXML
  private TextField cmdToSend;

  @FXML
  private ColorPicker colorPicker;

  @FXML
  private ToggleButton plugToggleButton;  // 插座开关

  @FXML
  ToggleButton lampSwitch;

  @FXML
  Slider light;

  public static BooleanProperty plugSelected;
  public static boolean status;
  public static String color;
  public static int lightValue = 3;

  /**
   * Controller 的声明周期方法，在这里进行一些初始化
   */
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    color = colorPicker.getValue().toString();
    // 存在对应的key时候才做初始化
    if (resources.containsKey("lamp.status")) {
      String lampStatus = resources.getString("lamp.status");
      status = Boolean.parseBoolean(lampStatus);
      color = resources.getString("lamp.color");
      colorPicker.setValue(Color.web(color));
      plugSelected = new SimpleBooleanProperty();
      plugSelected.bindBidirectional(plugToggleButton.selectedProperty());
    }
  }

  /**
   * 开 / 关插座
   */
  public void togglePlug(ActionEvent actionEvent) throws IOException {
    App.togglePlug();
  }

  /**
   * 更新网关彩灯颜色
   */
  public void updateColor(ActionEvent actionEvent) throws IOException {
    if (actionEvent.getSource().getClass().equals(JFXToggleButton.class)) {
      BooleanProperty booleanProperty = lampSwitch.selectedProperty();
      if (!booleanProperty.get()) {
        App.updateRGB(-1, getColor(), 0);
        status = false;
      } else {
        App.updateRGB(1, getColor(), lightValue);
        status = true;
      }
    } else {
      color = colorPicker.getValue().toString();
      App.updateRGB(0, getColor(), lightValue);
      status = true;
    }
  }

  /**
   * 从颜色选择器得到的颜色值，再转成表示 RGB 的整数
   */
  private int getColor() {
    String substring = color.substring(2, 8);
    return Integer.parseInt(substring, 16);
  }

  /**
   * 更新网关彩灯亮度
   */
  public void updateLight(MouseEvent dragEvent) throws IOException {
    lightValue = (int) light.getValue();
    App.updateRGB(2, getColor(), lightValue);
    status = true;
  }

  /**
   * 发送 whois 到组播地址
   */
  public void sendWhois(ActionEvent actionEvent) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    String toLocalMulticastAddress = objectMapper.writeValueAsString(new Whois());
    App.sendWhois(toLocalMulticastAddress);
  }

  /**
   * 发送任意内容到网关
   */
  public void sendWhatever(ActionEvent actionEvent) throws IOException {
    String toGateway = cmdToSend.getText();
    App.sendWhatever(toGateway);
  }
}
