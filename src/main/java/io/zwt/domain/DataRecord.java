package io.zwt.domain;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Attach 到 Selector 用的类
 */
public class DataRecord {
  public SocketAddress address;
  public ByteBuffer buffer;

  public DataRecord() {
    this.buffer = ByteBuffer.allocate(400);
  }
}
