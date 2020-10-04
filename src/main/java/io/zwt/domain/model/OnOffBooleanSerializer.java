package io.zwt.domain.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 一个自定义的序列化器类，用于将 true / false 序列化为 on / off
 */
public class OnOffBooleanSerializer extends JsonSerializer<Boolean> {

  protected static final String ON = "on";
  protected static final String OFF = "off";

  @Override
  public void serialize(Boolean value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeString(value ? ON : OFF);
  }
}
