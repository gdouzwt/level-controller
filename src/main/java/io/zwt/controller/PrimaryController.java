package io.zwt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zwt.App;
import io.zwt.domain.model.Whois;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * 主面板的控制器，界面交互逻辑写在这里。
 */
public class PrimaryController implements Initializable {

  @FXML
  Label label;

  @FXML
  ColorPicker colorPicker;

  @FXML
  Button button;

  @FXML
  Button lampSwitch;

  @FXML
  Slider light;

  public static boolean status;
  public static String color;
  public static int lightValue = 3;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    color = colorPicker.getValue().toString();
    // 存在对应的key时候才做初始化
    if (resources.containsKey("lamp.status")) {
      String lampStatus = resources.getString("lamp.status");
      status = Boolean.parseBoolean(lampStatus);
      color = resources.getString("lamp.color");
      colorPicker.setValue(Color.web(color));
      lampSwitch.setText(status ? "ON" : "OFF");
    }
  }

  public void togglePlug(ActionEvent actionEvent) throws IOException {
    App.togglePlug();
  }

  public void updateColor(ActionEvent actionEvent) throws IOException {
    if (actionEvent.getSource().getClass().equals(Button.class)) {
      if (status) {
        App.updateRGB(-1, getColor(), 0);
        status = false;
        lampSwitch.setText("OFF");
      } else {
        App.updateRGB(1, getColor(), lightValue);
        status = true;
        lampSwitch.setText("ON");
      }
    } else {
      color = colorPicker.getValue().toString();
      App.updateRGB(0, getColor(), lightValue);
      status = true;
      lampSwitch.setText("ON");
    }
  }

  private int getColor() {
    String substring = color.substring(2, 8);
    return Integer.parseInt(substring, 16);
  }

  public void updateLight(MouseEvent dragEvent) throws IOException {
    lightValue = (int) light.getValue();
    App.updateRGB(2, getColor(), lightValue);
    status = true;
    lampSwitch.setText("ON");
  }

  public void sendWhois(ActionEvent actionEvent) throws IOException {

    ObjectMapper objectMapper = new ObjectMapper();
    String s = objectMapper.writeValueAsString(new Whois());
    App.sendWhois(s);
  }
}
