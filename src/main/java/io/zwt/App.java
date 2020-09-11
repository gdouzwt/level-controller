package io.zwt;

import io.zwt.util.SymmetricEncryption;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

import static io.zwt.config.Config.*;

public class App extends Application {

    private static volatile String encryptedKey;
    private static boolean isOn = true;
    private static final InetSocketAddress UNICAST = new InetSocketAddress(GATEWAY_ADDRESS, PORT);
    private static final SecretKey SECRET_KEY = new SecretKeySpec(SECRET.getBytes(), 0, 16, "AES");
    private static DatagramChannel channel = null;

    public static void main(String[] args) throws Exception {

        NetworkInterface ni = NetworkInterface.getByName("eth6"); // ethernet
        InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS); // Multicast address
        if (!multicastAddress.isMulticastAddress()) { // Test if multicast address
            throw new IllegalArgumentException("Not a multicast address");
        }

        channel = DatagramChannel.open(StandardProtocolFamily.INET)
            .setOption(StandardSocketOptions.SO_REUSEADDR, true)
            .bind(new InetSocketAddress(PORT))
            .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);  // IPv4
        channel.join(multicastAddress, ni);  // !important
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ, new DataRecord());

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
                                String heartbeat = printBufferData(dataRecord.buffer);
                                /*try (var mongoClient = MongoClients.create("mongodb://localhost:27017")) {
                                MongoDatabase database = mongoClient.getDatabase("gateway");
                                MongoCollection<Document> collection = database.getCollection("heartbeat");
                                collection.insertOne(new Document(BasicDBObject.parse(heartbeat)));
                                }*/
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
                            //updateRGB((DatagramChannel) selectionKey.channel());
                            //togglePlug((DatagramChannel) selectionKey.channel());
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

    public static void updateRGB(int hexString) throws IOException {

        /*
        int[] musicId = {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29};
        int i = random.nextInt(musicId.length);
        String writeMusic = "{\"cmd\":\"write\",\"model\":\"gateway\",\"sid\":\"7811dcf981c4\",\"short_id\":0,\"data\":\"{\\\"mid\\\":" + musicId[i] + KEY_JSON_ATTR + encryptedKey + CMD_TRAILER;
        */

        String writeRGBData = NEW_RGB_CMD_HEAD + ((0xFFL << 24) | hexString) + KEY_JSON_ATTR + encryptedKey + CMD_TRAILER;
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

    private static synchronized String printBufferData(Buffer input) throws Exception {

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
            byte[] cipher = tokenAESCrypto(tokenString);
            encryptedKey = DatatypeConverter.printHexBinary(cipher);
            System.out.println("************************************************************* KEY UPDATED **************************************************************");
        }
        System.out.println(stringContent);
        if (stringContent.contains("Invalid key")) {
            encryptedKey = null;
        }
        return stringContent;
    }


    private static byte[] tokenAESCrypto(String token) throws Exception {
        return SymmetricEncryption.performAESEncryption(token, SECRET_KEY, INITIALIZATION_VECTOR);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("/fxml/main-pane.fxml"));
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.setTitle(APP_TITLE);
        stage.show();
    }

    static class DataRecord {
        public SocketAddress address;
        public ByteBuffer buffer = ByteBuffer.allocate(400);
    }
}
