package io.zwt.domain.model;

import java.io.Serializable;

public class Data implements Serializable {
  private String ip;

  public Data(String ipString) {
    int from = ipString.indexOf(':') + 2;
    int to = ipString.indexOf('}') - 1;
    this.ip = ipString.substring(from, to);
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  @Override
  public String toString() {
    return "{" + "\"" + "ip" + "\"" + ':' + "\"" + ip + "\"" + '}';
  }
}
