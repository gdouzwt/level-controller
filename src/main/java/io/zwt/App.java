package io.zwt;

import io.zwt.controller.HomeController;
import io.zwt.domain.DataRecord;
import io.zwt.service.LANTask;
import io.zwt.util.SymmetricEncryption;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;

import static io.zwt.config.Config.*;

public class App extends Application {

  public static volatile String encryptedKey;
  public static DatagramChannel channel = null;
  private ResourceBundle resourceBundle;
  public static String HOME = System.getProperty("user.home");
  static final Logger log = LoggerFactory.getLogger(App.class);
  LANTask task;  // 局域网通信的一些

  /**
   * JavaFX 应用的生命周期方法
   * 启动 JavaFX Application Thread 之前的一些初始化操作。不能操作 SceneGraph
   *
   * @throws Exception
   */
  @Override
  public void init() throws Exception {

    File file = new File(HOME + "/preference.properties");
    if (file.createNewFile()) {
      Properties properties = new Properties();
      properties.setProperty("lamp.status", "false");
      properties.setProperty("lamp.color", "0x008000ff");
      FileWriter fileWriter = new FileWriter(file);
      properties.store(fileWriter, "lamp");
    } else {
      resourceBundle = new PropertyResourceBundle(new FileReader(file));
    }
    resourceBundle = new PropertyResourceBundle(new FileReader(file));

    Selector selector = getSelector();
    task = new LANTask(selector, this);
    task.setDaemon(true);
    task.start();
  }

  /**
   * JavaFX 应用的生命周期方法
   * 应用从这里启动， JavaFX Application Thread
   *
   * @throws Exception
   */
  @Override
  public void start(Stage stage) throws Exception {
    Parent parent = FXMLLoader.load(getClass().getResource("/fxml/home.fxml"), resourceBundle);
    Scene scene = new Scene(parent);
    stage.setScene(scene);
    stage.setTitle(APP_TITLE);
    stage.show();
  }

  /**
   * JavaFX 应用的生命周期方法
   * 在这里放一些退出应用时进行收尾工作的逻辑，现在是将彩灯状态保存到 properties 文件
   *
   * @throws Exception
   */
  @Override
  public void stop() throws Exception {
    System.out.println("Saving preference...");
    try {
      Properties properties = new Properties();
      properties.setProperty("lamp.status", String.valueOf(HomeController.status));
      properties.setProperty("lamp.color", HomeController.color);
      FileWriter fileWriter = new FileWriter(new File(HOME + "/preference.properties"));
      properties.store(fileWriter, "lamp");
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
  }

  /**
   * 获取 NIO 的选择器
   *
   * @return 选择器
   * @throws IOException
   */
  private static Selector getSelector() throws IOException {
    NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
    Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
    Iterator<NetworkInterface> networkInterfaceIterator = networkInterfaces.asIterator();
    while (networkInterfaceIterator.hasNext()) {
      NetworkInterface next = networkInterfaceIterator.next();
      Optional<InetAddress> first = next.inetAddresses()
        .filter(inetAddress -> inetAddress.getHostAddress().contains("192.168.1."))
        .findAny();
      if (first.isPresent()) {
        ni = next;
        break;
      }
    }
    InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
    channel = DatagramChannel.open(StandardProtocolFamily.INET)
      .setOption(StandardSocketOptions.SO_REUSEADDR, true)
      .bind(new InetSocketAddress(UNICAST_PORT))
      .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);

    channel.join(multicastAddress, ni);
    channel.configureBlocking(false);
    Selector selector = Selector.open();
    channel.register(selector, SelectionKey.OP_READ, new DataRecord());
    return selector;
  }

  /**
   * 更新网关彩灯颜色和亮度
   *
   * @param mode       开还是关，需要更改
   * @param colorValue 颜色的数值
   * @param light      亮度？
   * @throws IOException
   */
  public static void updateRGB(final int mode, final int colorValue, final int light) throws IOException {
    String cmd;
    if (mode == -1) {
      cmd = NEW_RGB_CMD_HEAD + ((light << 24) | colorValue) + KEY_JSON_ATTR + encryptedKey + CMD_TRAILER;
    } else {
      cmd = NEW_RGB_CMD_HEAD + ((light << 24) | colorValue) + KEY_JSON_ATTR + encryptedKey + CMD_TRAILER;
    }
    ByteBuffer to = ByteBuffer.wrap(cmd.getBytes());
    if (encryptedKey != null) {
      channel.send(to, GATEWAY_UNICAST_ADDRESS);
      log.debug(cmd);
    }
  }

  /**
   * 发送 whois 命令到本地网络接口的 UDP 组播地址，等待网关回应其所在 IP 地址
   *
   * @param cmd 发送 whois
   * @throws IOException
   */
  public static void sendWhois(String cmd) throws IOException {
    ByteBuffer to = ByteBuffer.wrap(cmd.getBytes());
    // 发送到组播地址
    channel.send(to, LOCAL_MULTICAST_ADDRESS);
    log.debug(cmd);
  }

  /**
   * 发送任意内容
   *
   * @param cmd 要被发送的命令
   * @throws IOException
   */
  public static void sendWhatever(String cmd) throws IOException {
    ByteBuffer to = ByteBuffer.wrap(cmd.getBytes());
    channel.send(to, GATEWAY_UNICAST_ADDRESS);
    log.debug(cmd);
  }

  /**
   * 对米家智能插座进行开关控制
   *
   * @throws IOException
   */
  public static void togglePlug() throws IOException {
    // 不用状态量了，通过 plugSelected 属性来开关
    String cmd = WRITE_PLUG + (HomeController.plugSelected.get() ? ON : OFF) + KEY_JSON_ATTR + encryptedKey + CMD_TRAILER;
    ByteBuffer to = ByteBuffer.wrap(cmd.getBytes());
    if (encryptedKey != null) {
      channel.send(to, GATEWAY_UNICAST_ADDRESS);
    }
    log.debug(cmd);
  }

  /**
   * 到从网关接收到数据包时调用这个方法
   *
   * @param receivedBytes 接收到的 UDP 数据包的字节
   * @return 返回数据包内容的 String 表示
   * @throws Exception 如果有异常就往上抛
   */
  public String onReceiveData(ByteBuffer receivedBytes) throws Exception {
    receivedBytes.flip();
    byte[] bytes = new byte[receivedBytes.limit()];
    while (receivedBytes.hasRemaining()) {
      bytes[receivedBytes.position()] = receivedBytes.get();
    }
    String receivedString = new String(bytes);
    // 找到 token 并进行 AES-CBC 128 加密以获得 KEY
    if (receivedString.contains("gateway") && receivedString.contains("heartbeat")) {
      int tokenIndex = receivedString.indexOf("token");
      String tokenString = receivedString.substring(tokenIndex + 8, tokenIndex + 24);
      byte[] cipher = SymmetricEncryption.performAESEncryption(tokenString, SECRET_KEY, INITIALIZATION_VECTOR);
      encryptedKey = DatatypeConverter.printHexBinary(cipher);
      log.debug("updated key --> " + encryptedKey);
    }
    // 如果 KEY 失效（刚好不在 10s 的时间窗口内，或其它原因），则将 encryptedKey 置为空
    if (receivedString.contains("Invalid key")) {
      encryptedKey = null;
    }
    log.debug(receivedString);
    return receivedString;
  }
}
