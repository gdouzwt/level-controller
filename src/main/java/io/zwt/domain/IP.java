package io.zwt.domain;

import java.io.Serializable;

public class IP implements Serializable {
  private String ip;

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public IP(String ip) {
    int from = ip.indexOf(':') + 1;
    int to = ip.indexOf('}');
    this.ip = ip.substring(from, to);
  }

  @Override
  public String toString() {
    return ip;
  }
}
