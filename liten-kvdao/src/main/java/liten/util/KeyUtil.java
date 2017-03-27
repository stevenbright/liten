package liten.util;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class KeyUtil {
  private KeyUtil() {}

  public static int getKeyBodyIndex(String serviceName, String entityName, int minVersion, int maxVersion) {
    //IdCodec codec = SemanticIdCodec.forService(serviceName).withEntityName(entityName);
    //codec.decodeLong()

    throw new UnsupportedOperationException();
  }
}
