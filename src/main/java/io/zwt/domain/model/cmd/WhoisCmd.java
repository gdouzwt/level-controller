package io.zwt.domain.model.cmd;

import java.io.Serializable;

public class WhoisCmd implements Serializable {
  private final String cmd;

  public String getCmd() {
    return cmd;
  }

  public WhoisCmd() {
    this.cmd = "whois";
  }
}
