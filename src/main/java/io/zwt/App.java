package io.zwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zwt.controller.MainStageController;
import io.zwt.domain.Data;
import io.zwt.domain.DataRecord;
import io.zwt.domain.HeartBeat;
import io.zwt.domain.IP;
import io.zwt.service.LANTask;
import io.zwt.util.SymmetricEncryption;
import javafx.application.Application;
import javafx.beans.binding.When;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import javax.xml.bind.DatatypeConverter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.ResourceBundle;

import static io.zwt.config.Config.*;

public class App extends Application {

  public static volatile String encryptedKey;
  private static boolean isOn = true;
  private static DatagramChannel channel = null;
  private static Button button;
  LANTask task;

  @Override
  public void init() throws Exception {
    Selector selector = getSelector();
    App app = new App();
    task = new LANTask(selector, app);
    task.setDaemon(true);
    task.start();
  }

  @Override
  public void start(Stage stage) throws Exception {
    Parent parent = FXMLLoader.load(getClass().getResource("/fxml/main-pane.fxml"), ResourceBundle.getBundle("preference"));
    button = (Button) parent.lookup("#button");
    button.setBackground(new Background(new BackgroundFill(Paint.valueOf("grey"), new CornerRadii(12), null)));
    Label label = (Label) parent.lookup("#label");
    label.textProperty().bind(new When(task.heartBeatProperty().isNull())
      .then("Receiving data...")
      .otherwise(task.heartBeatProperty().asString()));
//    label.textProperty().bind(task.valueProperty());
    Scene scene = new Scene(parent);
    //scene.getStylesheets().add(getClass().getResource("/fxml/style.css").toString());
    stage.setScene(scene);
    stage.setTitle(APP_TITLE);
    stage.show();
  }

  @Override
  public void stop() throws Exception {
    System.out.println("Saving preference...");
    try {
      Properties properties = new Properties();
      properties.setProperty("lamp.status", String.valueOf(MainStageController.status));
      properties.setProperty("lamp.color", MainStageController.color);
      URL resource = getClass().getClassLoader().getResource("preference.properties");
      FileWriter fileWriter = new FileWriter(Paths.get(resource.toURI()).toFile());
      properties.store(fileWriter, "lamp");
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception {
    HeartBeat heartBeat = new HeartBeat();
    heartBeat.setCmd("heartbeat");
    heartBeat.setData("{\"ip\":\"192.168.1.145\"}");
    heartBeat.setModel("gateway");
    heartBeat.setSid("7811dcf981c4");
    heartBeat.setShortId("0");
    heartBeat.setToken("GNDLfCuo1JDcMwa5");
    ObjectMapper objectMapper = new ObjectMapper();
    String s = objectMapper.writeValueAsString(heartBeat);
    System.out.println(s);
    Application.launch(args);
  }

  private static Selector getSelector() throws IOException {
    NetworkInterface ni = NetworkInterface.getByName("eth6");
    InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
    channel = DatagramChannel.open(StandardProtocolFamily.INET)
      .setOption(StandardSocketOptions.SO_REUSEADDR, true)
      .bind(new InetSocketAddress(PORT))
      .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
    channel.join(multicastAddress, ni);
    channel.configureBlocking(false);
    Selector selector = Selector.open();
    channel.register(selector, SelectionKey.OP_READ, new DataRecord());
    return selector;
  }

  public static void updateRGB(final int cmd, final int colorValue) throws IOException {
    String writeRGBData;
    if (cmd == -1) {
      writeRGBData = NEW_RGB_CMD_HEAD + 0 + KEY_JSON_ATTR + encryptedKey + CMD_TRAILER;
    } else {
      writeRGBData = NEW_RGB_CMD_HEAD + ((0xFFL << 24) | colorValue) + KEY_JSON_ATTR + encryptedKey + CMD_TRAILER;
    }
    ByteBuffer to = ByteBuffer.wrap(writeRGBData.getBytes());
    if (encryptedKey != null) {
      channel.send(to, GATEWAY);
      System.out.println(writeRGBData);
    }
  }

  public static void togglePlug() throws IOException {
    isOn = !isOn;
    String writePlugData = WRITE_PLUG + (isOn ? OFF : ON) + KEY_JSON_ATTR + encryptedKey + CMD_TRAILER;
    ByteBuffer to = ByteBuffer.wrap(writePlugData.getBytes());
    System.out.println(writePlugData);
    if (encryptedKey != null) {
      channel.send(to, GATEWAY);
    }
  }

  public String onReceiveData(ByteBuffer receivedBytes) throws Exception {
    receivedBytes.flip();
    byte[] bytes = new byte[receivedBytes.limit()];
    while (receivedBytes.hasRemaining()) {
      bytes[receivedBytes.position()] = receivedBytes.get();
    }
    String receivedString = new String(bytes);
    // find token and encrypt it
    if (receivedString.contains("gateway") && receivedString.contains("heartbeat")) {
      int tokenIndex = receivedString.indexOf("token");
      String tokenString = receivedString.substring(tokenIndex + 8, tokenIndex + 24);
      byte[] cipher = SymmetricEncryption.performAESEncryption(tokenString, SECRET_KEY, INITIALIZATION_VECTOR);
      encryptedKey = DatatypeConverter.printHexBinary(cipher);
      System.out.println(KEY_UPDATED);
    }
    /*if (receivedString.contains("report") && receivedString.contains("plug")) {
      if (receivedString.contains("on")) {
        Platform.runLater(() -> {
          button.setBackground(new Background(new BackgroundFill(Paint.valueOf("green"), new CornerRadii(12), null)));
          button.setTextFill(Paint.valueOf("white"));
          button.setText("插座已开");
        });
      } else {
        Platform.runLater(() -> {
          button.setBackground(new Background(new BackgroundFill(Paint.valueOf("grey"), new CornerRadii(12), null)));
          button.setTextFill(Paint.valueOf("black"));
          button.setText("插座已关");
        });
      }
    }*/
    if (receivedString.contains("Invalid key")) {
      encryptedKey = null;
    }
    System.out.println(receivedString);
    return receivedString;
  }
}
