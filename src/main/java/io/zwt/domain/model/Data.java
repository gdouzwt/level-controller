package io.zwt.domain.model;

import java.io.Serializable;

public class Data implements Serializable {
  private String content;

  public Data(String content) {
    /*int from = ipString.indexOf(':') + 2;
    int to = ipString.indexOf('}') - 1;
    this.content = ipString.substring(from, to);*/
    this.content = content;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public String toString() {
    //return "{" + "\"" + "ip" + "\"" + ':' + "\"" + content + "\"" + '}';
    return content;
  }
}
