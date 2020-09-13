package io.zwt.config;

/**
 * 保存一些常量，应该外部化配置，存到 properties 文件
 */

public class Config {
    public static final String KEY_UPDATED = "************************************************************* KEY UPDATED **************************************************************";
    public static final String ON = "\\\"on\\\"";
    public static final String OFF = "\\\"off\\\"";
    public static final String WRITE_PLUG = "{\"cmd\":\"write\",\"model\": \"plug\",\"sid\":\"158d000234727c\",\"short_id\":38455,\"data\": \"{\\\"status\\\":";
    public static final String NEW_RGB_CMD_HEAD = "{\"cmd\":\"write\",\"sid\":\"7811dcf981c4\",\"short_id\":0,\"data\":\"{\\\"rgb\\\":";
    public static final String KEY_JSON_ATTR = ",\\\"key\\\":\\\"";
    public static final String CMD_TRAILER = "\\\"}\"}";
    public static final String SECRET = "07wjrkc41typdvae";
    public static final int PORT = 9898;
    public static final String APP_TITLE = "骚气的米家智能网关控制面板";
    public static final String GATEWAY_ADDRESS = "192.168.1.145";
    public static final String MULTICAST_ADDRESS = "224.0.0.50";
    public static final byte[] INITIALIZATION_VECTOR = {0x17, (byte) 0x99, 0x6d, 0x09, 0x3d, 0x28, (byte) 0xdd, (byte) 0xb3, (byte) 0xba, 0x69, 0x5a, 0x2e, 0x6f, 0x58, 0x56, 0x2e};
}
