module io.zwt.ui {

  requires java.xml.bind;
  requires javafx.graphics;
  requires javafx.fxml;
  requires org.slf4j;
  requires org.slf4j.jul;
  requires com.fasterxml.jackson.databind;
  requires com.jfoenix;
  requires org.controlsfx.controls;

  exports io.zwt;
  opens io.zwt.domain.model to com.fasterxml.jackson.databind;
  opens io.zwt.domain.model.cmd to com.fasterxml.jackson.databind;
  opens io.zwt.domain.model.data to com.fasterxml.jackson.databind;
  opens io.zwt.controller to javafx.fxml, com.jfoenix, org.controlsfx.controls;
}
