package io.zwt.domain.model.data;

import com.fasterxml.jackson.annotation.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"cmd", "model", "sid", "short_id", "token", "data"})
public class GenericData implements Serializable {
  private final StringProperty cmd;
  private final StringProperty model;
  private final StringProperty sid;
  //@JsonSerialize(using = ToStringSerializer.class)
  private Integer shortId;
  private final StringProperty token;

  private String illumination;
  private Integer mid;
  private String joinPermission;
  private String removeDevice;
  private String level;

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getIllumination() {
    return illumination;
  }

  public void setIllumination(String illumination) {
    this.illumination = illumination;
  }

  public Integer getMid() {
    return mid;
  }

  public void setMid(Integer mid) {
    this.mid = mid;
  }

  public String getJoinPermission() {
    return joinPermission;
  }

  public void setJoinPermission(String joinPermission) {
    this.joinPermission = joinPermission;
  }

  public String getRemoveDevice() {
    return removeDevice;
  }

  public void setRemoveDevice(String removeDevice) {
    this.removeDevice = removeDevice;
  }

  @JsonRawValue(value = false)
  private String data;

  public GenericData() {
    this.cmd = new SimpleStringProperty();
    this.model = new SimpleStringProperty();
    this.sid = new SimpleStringProperty();
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
    return shortId;
  }

  public void setShortId(Integer shortId) {
    this.shortId = shortId;
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

}
