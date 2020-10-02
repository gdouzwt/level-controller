package io.zwt.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"cmd", "model", "sid", "short_id", "token", "data"})
public class HeartBeat implements Serializable {
  private StringProperty cmd;
  private StringProperty model;
  private StringProperty sid;
  //@JsonSerialize(using = ToStringSerializer.class)
  private IntegerProperty shortId;
  private StringProperty token;
  @JsonRawValue(value = false)
  private String data;

  public HeartBeat() {
    this.cmd = new SimpleStringProperty();
    this.model = new SimpleStringProperty();
    this.sid = new SimpleStringProperty();
    this.shortId = new SimpleIntegerProperty();
    this.token = new SimpleStringProperty();
  }

  public String getCmd() {
    return cmd.get();
  }

  public StringProperty cmdProperty() {
    return cmd;
  }

  public void setCmd(String cmd) {
    this.cmdProperty().set(cmd);
  }

  public String getModel() {
    return model.get();
  }

  public StringProperty modelProperty() {
    return model;
  }

  public void setModel(String model) {
    this.modelProperty().set(model);
  }

  public String getSid() {
    return sid.get();
  }

  public StringProperty sidProperty() {
    return sid;
  }

  public void setSid(String sid) {
    this.sidProperty().set(sid);
  }

  @JsonProperty("short_id")
  public Integer getShortId() {
    return shortId.get();
  }

  public IntegerProperty shortIdProperty() {
    return shortId;
  }

  public void setShortId(Integer shortId) {
    this.shortIdProperty().set(shortId);
  }

  public String getToken() {
    return token.get();
  }

  public StringProperty tokenProperty() {
    return token;
  }

  public void setToken(String token) {
    this.tokenProperty().set(token);
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return "HeartBeat {" +
      "cmd='" + cmd.get() + '\'' +
      ", model='" + model.get() + '\'' +
      ", sid='" + sid.get() + '\'' +
      ", shortId='" + shortId.get() + '\'' +
      ", token='" + token.get() + '\'' +
      ", data=" + data +
      '}';
  }
}
