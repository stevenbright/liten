package liten.catalog.dao.support;

import com.google.protobuf.InvalidProtocolBufferException;
import com.truward.semantic.id.IdCodec;
import com.truward.semantic.id.SemanticIdCodec;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.model.Ise;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class DefaultIseCatalogDao implements IseCatalogDao {
  private static final IdCodec ITEM_CODEC = SemanticIdCodec.forPrefixNames("S1");

  private final Environment environment;
  private final Store itemStore;

  public DefaultIseCatalogDao(Environment environment) {
    this.environment = environment;
    this.itemStore = environment.computeInTransaction(tx ->
        environment.openStore("", StoreConfig.WITHOUT_DUPLICATES, tx));
  }

  @Override
  public Ise.Item getById(String id) {
    final byte[] key = ITEM_CODEC.decodeBytes(id);

    return environment.computeInTransaction(tx -> {
      final ByteIterable val = itemStore.get(tx, new ArrayByteIterable(key));
      //Ise.Item.getDefaultInstance().getParserForType().parseFrom()
      try {
        return Ise.Item.parseFrom(val.getBytesUnsafe());
      } catch (InvalidProtocolBufferException e) {
        throw new IllegalStateException(e);
      }
    });
  }

  @Override
  public List<String> getNameHints(String prefix) {
    return null;
  }
}
