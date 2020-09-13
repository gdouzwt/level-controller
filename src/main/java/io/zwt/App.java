package io.zwt;

import io.zwt.controller.AppController;
import io.zwt.util.SymmetricEncryption;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;

import static io.zwt.config.Config.*;

public class App extends Application {

    private static volatile String encryptedKey;
    private static boolean isOn = true;
    private static final InetSocketAddress UNICAST = new InetSocketAddress(GATEWAY_ADDRESS, PORT);
    private static final SecretKey SECRET_KEY = new SecretKeySpec(SECRET.getBytes(), 0, 16, "AES");
    private static DatagramChannel channel = null;
    private static Button button;

    public static void main(String[] args) throws Exception {

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
        App app = new App();

        Thread backgroundTask = new Thread(() -> {
            while (true) {
                try {
                    if (selector.select(10000) == 0) {
                        System.out.println("Waiting for heartbeat sync...");
                        continue;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext(); ) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isReadable()) {
                        DatagramChannel selectedChannel = (DatagramChannel) selectionKey.channel();
                        DataRecord dataRecord = (DataRecord) selectionKey.attachment();
                        dataRecord.buffer.clear();
                        try {
                            dataRecord.address = selectedChannel.receive(dataRecord.buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (dataRecord.address != null) {
                            try {
                                app.onReceiveData(dataRecord.buffer);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (encryptedKey != null) {
                                selectionKey.interestOps(SelectionKey.OP_WRITE);
                            }
                        }
                    }
                    if (selectionKey.isValid() && selectionKey.isWritable()) {
                        if (encryptedKey == null) {
                            selectionKey.interestOps(SelectionKey.OP_READ);
                            break;
                        } else {
                            selectionKey.interestOps(SelectionKey.OP_READ);
                        }
                    }
                    iterator.remove();
                }
            }
        });

        backgroundTask.setDaemon(true);
        backgroundTask.start();

        launch(args);
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
            channel.send(to, UNICAST);
            System.out.println(writeRGBData);
        }
    }

    public static void togglePlug() throws IOException {
        isOn = !isOn;
        String writePlugData = WRITE_PLUG + (isOn ? OFF : ON) + KEY_JSON_ATTR + encryptedKey + CMD_TRAILER;
        ByteBuffer to = ByteBuffer.wrap(writePlugData.getBytes());
        System.out.println(writePlugData);
        if (encryptedKey != null) {
            channel.send(to, UNICAST);
        }
    }

    private synchronized String onReceiveData(Buffer input) throws Exception {

        input.flip();
        byte[] content = new byte[input.limit()];
        while (input.hasRemaining()) {
            content[input.position()] = ((ByteBuffer) input).get();
        }
        String stringContent = new String(content);
        // find token and encrypt it
        if (stringContent.contains("gateway") && stringContent.contains("heartbeat")) {
            int token = stringContent.indexOf("token");
            token += 8;
            String tokenString = stringContent.substring(token, token + 16);
            byte[] cipher = encryptToken(tokenString);
            encryptedKey = DatatypeConverter.printHexBinary(cipher);
            System.out.println(KEY_UPDATED);
        }
        System.out.println(stringContent);
        if (stringContent.contains("report") && stringContent.contains("plug")) {
            if (stringContent.contains("on")) {
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
        }
        if (stringContent.contains("Invalid key")) {
            encryptedKey = null;
        }
        return stringContent;
    }

    private static byte[] encryptToken(String token) throws Exception {
        return SymmetricEncryption.performAESEncryption(token, SECRET_KEY, INITIALIZATION_VECTOR);
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Saving preference...");
        try {
            Properties properties = new Properties();
            properties.setProperty("lamp.status", String.valueOf(AppController.status));
            properties.setProperty("lamp.color", AppController.color);
            URL resource = getClass().getClassLoader().getResource("preference.properties");
            FileWriter fileWriter = new FileWriter(Paths.get(resource.toURI()).toFile());
            properties.store(fileWriter, "lamp");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("/fxml/main-pane.fxml"), ResourceBundle.getBundle("preference"));
        button = (Button) parent.lookup("#button");
        button.setBackground(new Background(new BackgroundFill(Paint.valueOf("grey"), new CornerRadii(12), null)));
        Scene scene = new Scene(parent);
        //scene.getStylesheets().add(getClass().getResource("/fxml/style.css").toString());
        stage.setScene(scene);
        stage.setTitle(APP_TITLE);
        stage.show();
    }

    static class DataRecord {
        public SocketAddress address;
        public ByteBuffer buffer = ByteBuffer.allocate(400);
    }
}
