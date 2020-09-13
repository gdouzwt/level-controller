package io.zwt.service;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

public class LANTask extends Task<StringProperty> {

  @Override
  protected StringProperty call() throws Exception {
    StringProperty stringProperty = new SimpleStringProperty();

    updateValue(stringProperty);
    return stringProperty;
  }
}
