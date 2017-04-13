package com.truward.xodus.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.truward.dao.ItemDeserializationException;
import com.truward.dao.ItemNotFoundException;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Protocol buffer entity de/serializer.
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class ProtoEntity {
  private ProtoEntity() {} // hidden

  @SuppressWarnings("unchecked")
  public static <T extends Message> T entryToProto(@Nullable ByteIterable byteIterable, T defaultInstance) {
    if (byteIterable == null) {
      throw new ItemNotFoundException(defaultInstance.getClass().getName());
    }

    final byte[] bytes = byteIterable.getBytesUnsafe();
    if (bytes == null) {
      throw new ItemDeserializationException("Can't get bytes for parsing entity " + defaultInstance.getClass());
    }

    try {
      return (T) defaultInstance.getParserForType().parseFrom(bytes, 0, byteIterable.getLength());
    } catch (InvalidProtocolBufferException e) {
      throw new ItemDeserializationException("Can't parse entity " + defaultInstance.getClass(), e);
    }
  }

  public static <T extends Message> ByteIterable protoToEntry(T instance) {
    return new ArrayByteIterable(instance.toByteArray());
  }
}
