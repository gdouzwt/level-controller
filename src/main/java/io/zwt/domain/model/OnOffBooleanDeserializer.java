package io.zwt.domain.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * 一个自定义的反序列化器类，用于将 on / off 反序列化为 true / false
 */
public class OnOffBooleanDeserializer extends JsonDeserializer<Boolean> {

  protected static final String ON = "on";
  protected static final String OFF = "off";


  @Override
  public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    JsonToken currentToken = p.getCurrentToken();

    if (currentToken.equals(JsonToken.VALUE_STRING)) {
      String text = p.getText().trim();

      if (ON.equalsIgnoreCase(text)) {
        return Boolean.TRUE;
      } else if (OFF.equalsIgnoreCase(text)) {
        return Boolean.FALSE;
      }
      throw ctxt.weirdStringException(text, Boolean.class,
        "Only \"" + ON + "\" or \"" + OFF + "\" values supported");
    } else if (currentToken.equals(JsonToken.VALUE_NULL)) {
      return getNullValue(ctxt);
    }

    throw ctxt.mappingException(Boolean.class);
  }
}
