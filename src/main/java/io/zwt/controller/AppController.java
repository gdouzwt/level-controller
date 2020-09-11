package io.zwt.controller;

import io.zwt.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void togglePlug(ActionEvent actionEvent) throws IOException {
        App.togglePlug();
    }

    public void updateColor(ActionEvent actionEvent) throws IOException {
        Color value = colorPicker.getValue();
        String s = value.toString();
        String substring = s.substring(2, 8);
        int i = Integer.parseInt(substring, 16);
        App.updateRGB(i);
    }
}
