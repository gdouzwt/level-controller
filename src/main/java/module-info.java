module io.zwt.ui {

  requires java.xml.bind;
  requires javafx.graphics;
  requires javafx.fxml;
  requires javafx.controls;
  requires org.eclipse.paho.client.mqttv3;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.annotation;

  opens io.zwt to javafx.graphics;
  opens io.zwt.domain.model to com.fasterxml.jackson.databind;
  opens io.zwt.controller to javafx.fxml, javafx.controls, javafx.graphics;
}
