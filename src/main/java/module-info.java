module io.zwt {

  requires java.xml.bind;
  requires javafx.graphics;
  requires javafx.controls;
  requires javafx.fxml;
  //requires mongo.java.driver;
  //requires org.apache.logging.log4j;
  opens io.zwt to javafx.fxml, javafx.controls, javafx.graphics, java.xml.bind;
  opens io.zwt.controller to javafx.fxml, javafx.controls, javafx.graphics;
}
