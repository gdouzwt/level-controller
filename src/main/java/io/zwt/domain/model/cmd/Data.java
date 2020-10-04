package io.zwt.domain.model.cmd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.zwt.domain.model.OnOffBooleanSerializer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Data implements Serializable {

  @JsonSerialize(using = OnOffBooleanSerializer.class)
  private final BooleanProperty status;
  private final IntegerProperty rgb;
  private String key;

  public Data() {
    this.status = new SimpleBooleanProperty();
    this.rgb = new SimpleIntegerProperty();
  }

  public boolean isStatus() {
    return status.get();
  }

  public BooleanProperty statusProperty() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status.set(status);
  }

  public int getRgb() {
    return rgb.get();
  }

  public IntegerProperty rgbProperty() {
    return rgb;
  }

  public void setRgb(int rgb) {
    this.rgb.set(rgb);
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @Override
  public String toString() {
    return "Data{" +
      "status=" + status +
      ", rgb=" + rgb +
      ", key='" + key + '\'' +
      '}';
  }
}
