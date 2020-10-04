package io.zwt.domain.model.cmd;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class IAm implements Serializable {

  private String cmd;
  private String port;
  private String sid;
  private String model;
  @JsonProperty("proto_version")
  private String protoVersion;
  private String ip;

  public String getCmd() {
    return cmd;
  }

  public void setCmd(String cmd) {
    this.cmd = cmd;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getSid() {
    return sid;
  }

  public void setSid(String sid) {
    this.sid = sid;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getProtoVersion() {
    return protoVersion;
  }

  public void setProtoVersion(String protoVersion) {
    this.protoVersion = protoVersion;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  @Override
  public String toString() {
    return "IAm {" +
      "cmd='" + cmd + '\'' +
      ", port='" + port + '\'' +
      ", sid='" + sid + '\'' +
      ", model='" + model + '\'' +
      ", protoVersion='" + protoVersion + '\'' +
      ", ip='" + ip + '\'' +
      '}';
  }
}
