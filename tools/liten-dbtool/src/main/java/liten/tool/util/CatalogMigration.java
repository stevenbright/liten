package liten.tool.util;

import com.truward.kvdao.xodus.metadata.MetadataDao;
import com.truward.xodus.migration.MigrationContext;
import com.truward.xodus.migration.MigrationExecutor;
import com.truward.xodus.migration.MigrationUtil;
import jetbrains.exodus.env.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class CatalogMigration {

  private static final String VERSION_0 = "0.0";
  private static final String VERSION_1 = "1.0";

  private static final String ITEM_STORE_NAME = "item";
  private static final String EXTERNAL_ID_STORE_NAME = "external-id";
  private static final String FORWARD_RELATIONS_STORE_NAME = "forward-relations";

  private static final int TRANSFER_BLOCK_SIZE = 1024;

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final MigrationExecutor migrationExecutor;

  public CatalogMigration(File sourceDirectory, File targetDirectory) {
    this.migrationExecutor = MigrationExecutor.newBuilder()
        .add(VERSION_0, this::upgradeToVersion1)
        .addLast(VERSION_1)
        .setInitialVersion(VERSION_0)
        .setSourceDirectory(sourceDirectory)
        .setTargetDirectory(targetDirectory)
        .build();
  }

  //
  // Private
  //

  private void upgrade() {
    boolean migrated = migrationExecutor.migrate();
    if (!migrated) {
      throw new IllegalStateException("Unexpected: migration failed due to missing non-last version entries");
    }
  }

  private static class StoresV0 {
    final Store item;
    final Store externalId;
    final Store forwardRelations;

    StoresV0(Environment environment, Transaction tx) {
      // bytesFromSemanticId(item.id) -> item
      this.item = environment.openStore(ITEM_STORE_NAME, StoreConfig.WITHOUT_DUPLICATES, tx);

      // ExternalId -> item.id(semantic ID)
      this.externalId = environment.openStore(EXTERNAL_ID_STORE_NAME, StoreConfig.WITHOUT_DUPLICATES, tx);

      // ForwardRelationId -> item.id(semantic ID)
      // e.g.:
      //      fromItemId=SomeAuthorId, type=author -> bookId
      this.forwardRelations = environment.openStore(FORWARD_RELATIONS_STORE_NAME, StoreConfig.WITH_DUPLICATES, tx);
    }
  }

  // V1 stores are the same as V0 ones, in addition they have name indices
  private static class StoresV1 extends StoresV0 {
    final Store itemByName;

    StoresV1(Environment environment, Transaction tx) {
      super(environment, tx);

      this.itemByName = environment.openStore("item-by-name", StoreConfig.WITHOUT_DUPLICATES, tx);
    }
  }

  private void upgradeToVersion1(MigrationContext ctx) {
    final Environment s = ctx.getSourceEnvironment();
    final Environment t = ctx.getTargetEnvironment();

    final StoresV0 srcStores = s.computeInExclusiveTransaction(tx -> new StoresV0(s, tx));
    final StoresV1 tgtStores = t.computeInExclusiveTransaction(tx -> new StoresV1(t, tx));

    // copy stores that shall be transferred "as is"
    int transferred = MigrationUtil.copyStores(srcStores.item, tgtStores.item);
    log.info("DB transfer completed, database: item, transferred={}", transferred);

    transferred = MigrationUtil.copyStores(srcStores.externalId, tgtStores.externalId);
    log.info("DB transfer completed, database: externalId, transferred={}", transferred);

    transferred = MigrationUtil.copyStores(srcStores.forwardRelations, tgtStores.forwardRelations);
    log.info("DB transfer completed, database: forwardRelations, transferred={}", transferred);
  }
}
