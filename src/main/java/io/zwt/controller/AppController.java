package io.zwt.controller;

import io.zwt.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * 主面板的控制器，界面交互逻辑写在这里。
 */
public class AppController implements Initializable {

  @FXML
  ColorPicker colorPicker;

  @FXML
  Button button;

  @FXML
  Button lampSwitch;

  public static boolean status;
  public static String color;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

    String lampStatus = resources.getString("lamp.status");
    color = resources.getString("lamp.color");
    status = Boolean.parseBoolean(lampStatus);
    colorPicker.setValue(Color.web(color));
    lampSwitch.setText(status ? "ON" : "OFF");
  }

  public void togglePlug(ActionEvent actionEvent) throws IOException {
    App.togglePlug();
  }

  public void updateColor(ActionEvent actionEvent) throws IOException {
    if (actionEvent.getSource().getClass().equals(Button.class)) {
      if (status) {
        App.updateRGB(-1, getColor());
        status = false;
        lampSwitch.setText("OFF");
      } else {
        App.updateRGB(0, getColor());
        status = true;
        lampSwitch.setText("ON");
      }
    } else {
      color = colorPicker.getValue().toString();
      App.updateRGB(1, getColor());
      status = true;
      lampSwitch.setText("ON");
    }
  }

  private int getColor() {
    String substring = color.substring(2, 8);
    return Integer.parseInt(substring, 16);
  }
}
