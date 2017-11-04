package liten.tool.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.truward.kvdao.xodus.metadata.MetadataDao;
import jetbrains.exodus.env.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class CatalogMigration {
  private static final String VERSION_0 = "0.0";
  private static final String VERSION_1 = "1.0";

  private static final String ITEM_STORE_NAME = "item";
  private static final String EXTERNAL_ID_STORE_NAME = "external-id";
  private static final String FORWARD_RELATIONS_STORE_NAME = "forward-relations";

  private static final int TRANSFER_BLOCK_SIZE = 1024;

  private static final Map<String, Consumer<CatalogMigration>> VERSION_UPGRADE_MAP =
      ImmutableMap.<String, Consumer<CatalogMigration>>builder()
          .put(VERSION_0, CatalogMigration::upgradeToVersion1)
          .build();

  private static final List<String> VERSIONS = ImmutableList.of(
      VERSION_0,
      VERSION_1
  );

  static {
    // TODO: use version builder and fold version list and map!
    // make sure that all versions are unique and lexicographically increasing
    for (int i = 1; i < VERSIONS.size(); ++i) {
      final String prev = VERSIONS.get(i - 1);
      final String cur = VERSIONS.get(i);
      final int cmp = cur.compareTo(prev);
      if (cmp == 0) {
        throw new AssertionError("Duplicate version=" + cur + " at pos=" + i);
      }
      if (cmp < 0) {
        throw new AssertionError("Non lexicographically increasing version=" + cur +
            " found at pos=" + i + ", previous version=" + prev);
      }
    }

    // static validation of versions
    final Set<String> mismatchedVersions = Sets.intersection(
        VERSION_UPGRADE_MAP.keySet(),
        ImmutableSet.of(VERSIONS.subList(0, VERSIONS.size() - 1)));
    if (!mismatchedVersions.isEmpty()) {
      throw new AssertionError("Mismatched versions in the upgrade map and versions list: " +
          mismatchedVersions);
    }
  }

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final Environment sourceEnv;
  private final Environment targetEnv;

  public CatalogMigration(Environment sourceEnv, Environment targetEnv) {
    this.sourceEnv = requireNonNull(sourceEnv);
    this.targetEnv = requireNonNull(targetEnv);
  }

  public void upgrade() {
    final String sourceVersion = readSourceVersion();
    final Consumer<CatalogMigration> upgradeFunction = VERSION_UPGRADE_MAP.get(sourceVersion);
    if (upgradeFunction == null) {
      throw new IllegalStateException("Database of version=" + sourceVersion + " can not be upgraded");
    }

    log.info("Upgrading version={}", sourceVersion);
    upgradeFunction.accept(this);
  }

  //
  // Private
  //

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

  private void upgradeToVersion1() {
    final Environment s = this.sourceEnv;
    final Environment t = this.targetEnv;

    final StoresV0 srcStores = s.computeInExclusiveTransaction(tx -> new StoresV0(s, tx));
    final StoresV1 tgtStores = t.computeInExclusiveTransaction(tx -> new StoresV1(t, tx));

    // copy stores that shall be transferred "as is"
    copyStores(srcStores.item, tgtStores.item, log);
    copyStores(srcStores.externalId, tgtStores.externalId, log);
    copyStores(srcStores.forwardRelations, tgtStores.forwardRelations, log);

    // lastly, add metadata table with version 1
    putVersion(VERSION_1);
  }

  private static void copyStores(Store source, Store target, Logger log) {
    final int total = source.getEnvironment().computeInTransaction(sourceTx ->
        target.getEnvironment().computeInTransaction(targetTx -> {
          final Cursor sourceCursor = source.openCursor(sourceTx);
          int transferred = 0;

          while (sourceCursor.getNext()) {
            target.add(targetTx, sourceCursor.getKey(), sourceCursor.getValue());
            ++transferred;
          }

          return transferred;
        }));

    log.info("Transferred {} records into store {}", total, target.getName());
  }

  private void putVersion(String version) {
    final MetadataDao metadataDao = new MetadataDao(targetEnv);
    targetEnv.executeInTransaction(tx -> {
      metadataDao.create(tx, version);
    });
  }

  private String readSourceVersion() {
    final MetadataDao metadataDao = new MetadataDao(sourceEnv);
    final Optional<String> version = sourceEnv.computeInTransaction(metadataDao::getVersion);
    return version.orElse(VERSION_0);
  }
}
