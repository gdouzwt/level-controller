package io.zwt;

/**
 * 用于启动 JavaFX 应用程序的类。
 */
public class Launcher {
  public static void main(String[] args) {
    System.setProperty("java.security.policy", "/java.policy");
    App.launch(App.class, args);
  }
}
