package liten.dbtool;

import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.TextFormat;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public final class DumperTest {

  @Test
  public void shouldConvertToTextFormatAndViceVersa() throws IOException {
    // Given:
    final Message message = StringValue.newBuilder().setValue("Test").build();
    final StringBuilder sb = new StringBuilder();

    // When:
    TextFormat.print(message, sb);

    // Then:
    final String textDump = sb.toString();
    final StringValue.Builder newMessageBuilder = StringValue.newBuilder();
    TextFormat.getParser().merge(textDump, newMessageBuilder);

    assertEquals(message, newMessageBuilder.build());
  }
}
