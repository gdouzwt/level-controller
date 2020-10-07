package io.zwt.domain.model.data;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

public class PlugReadAckData implements Serializable {

  private IntegerProperty voltage;
  private StringProperty status;
  private StringProperty inuse;
  private StringProperty powerConsumed;
  private StringProperty loadPower;

  public int getVoltage() {
    return voltage.get();
  }

  public IntegerProperty voltageProperty() {
    return voltage;
  }

  public void setVoltage(int voltage) {
    this.voltage.set(voltage);
  }

  public String getStatus() {
    return status.get();
  }

  public StringProperty statusProperty() {
    return status;
  }

  public void setStatus(String status) {
    this.status.set(status);
  }

  public String getInuse() {
    return inuse.get();
  }

  public StringProperty inuseProperty() {
    return inuse;
  }

  public void setInuse(String inuse) {
    this.inuse.set(inuse);
  }

  public String getPowerConsumed() {
    return powerConsumed.get();
  }

  public StringProperty powerConsumedProperty() {
    return powerConsumed;
  }

  public void setPowerConsumed(String powerConsumed) {
    this.powerConsumed.set(powerConsumed);
  }

  public String getLoadPower() {
    return loadPower.get();
  }

  public StringProperty loadPowerProperty() {
    return loadPower;
  }

  public void setLoadPower(String loadPower) {
    this.loadPower.set(loadPower);
  }

  @Override
  public String toString() {
    return "PlugReadAck {" +
      "voltage=" + voltage +
      ", status=" + status +
      ", inuse=" + inuse +
      ", powerConsumed=" + powerConsumed +
      ", loadPower=" + loadPower +
      '}';
  }
}
