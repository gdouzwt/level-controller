module io.zwt {

  requires java.xml.bind;
  requires javafx.graphics;
  requires javafx.controls;
  requires javafx.fxml;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.annotation;
  //requires mongo.java.driver;
  //requires org.apache.logging.log4j;
  opens io.zwt to javafx.graphics, java.xml.bind;
  opens io.zwt.domain to com.fasterxml.jackson.databind;
  opens io.zwt.controller to javafx.fxml, javafx.controls, javafx.graphics;
}
