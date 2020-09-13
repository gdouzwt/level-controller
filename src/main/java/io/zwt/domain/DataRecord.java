package io.zwt.domain;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class DataRecord {
  public SocketAddress address;
  public ByteBuffer buffer;

  public DataRecord() {
    this.buffer = ByteBuffer.allocate(400);
  }
}
