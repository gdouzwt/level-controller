package io.zwt.domain.model.cmd;

import com.fasterxml.jackson.annotation.JsonRawValue;

import java.io.Serializable;

public class WriteCmd implements Serializable {
  private String cmd;
  private String sid;
  @JsonRawValue(value = false)
  private String data;

  public WriteCmd() {
    this.cmd = "write";
  }

  public String getCmd() {
    return cmd;
  }

  public String getSid() {
    return sid;
  }

  public void setSid(String sid) {
    this.sid = sid;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return "WriteCmd {" +
      "cmd='" + cmd + '\'' +
      ", sid='" + sid + '\'' +
      ", data='" + data + '\'' +
      '}';
  }
}
