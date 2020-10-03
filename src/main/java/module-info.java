module io.zwt.ui {

  requires java.xml.bind;
  requires javafx.graphics;
  requires javafx.fxml;
  requires javafx.controls;
  requires org.slf4j;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.annotation;
  requires com.jfoenix;

  exports io.zwt;
  //opens io.zwt to javafx.graphics, com.jfoenix;
  opens io.zwt.domain.model to com.fasterxml.jackson.databind;
  opens io.zwt.domain.model.cmd to com.fasterxml.jackson.databind;
  opens io.zwt.domain.model.data to com.fasterxml.jackson.databind;
  opens io.zwt.controller to javafx.fxml, javafx.controls, javafx.graphics, com.jfoenix;
}
