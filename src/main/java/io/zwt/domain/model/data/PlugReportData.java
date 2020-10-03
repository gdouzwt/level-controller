package io.zwt.domain.model.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.zwt.domain.model.OnOffBooleanDeserializer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlugReportData implements Serializable {

  public PlugReportData() {
    this.status = new SimpleBooleanProperty();
  }

  @JsonDeserialize(using = OnOffBooleanDeserializer.class)
  private BooleanProperty status;

  public boolean isStatus() {
    return status.get();
  }

  public BooleanProperty statusProperty() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status.set(status);
  }
}
