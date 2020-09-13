package io.zwt.domain;

import java.io.Serializable;

public class Whois implements Serializable {
  private final String cmd;

  public String getCmd() {
    return cmd;
  }

  public Whois() {
    this.cmd = "whois";
  }
}
