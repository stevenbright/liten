package liten.catalog.dao.support;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.truward.protobuf.jackson.ProtobufJacksonUtil;
import jetbrains.exodus.env.Environment;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.model.Ise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper class for working with large portions of data.
 */
@ParametersAreNonnullByDefault
public final class IseCatalogIngestionHelper {
  private static final JsonFactory DEFAULT_JSON_FACTORY = new JsonFactory();
  private static final int BULK_PERSIST_COUNT = 32;

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final Environment environment;
  private final IseCatalogDao catalogDao;

  public IseCatalogIngestionHelper(Environment environment, IseCatalogDao catalogDao) {
    this.environment = environment;
    this.catalogDao = catalogDao;
  }

  public void importData(InputStream inputStream) throws IOException {
    // prepare data dump
    final Ise.Dump dump;
    try (final JsonParser jp = DEFAULT_JSON_FACTORY.createParser(inputStream)) {
      dump = ProtobufJacksonUtil.readJson(Ise.Dump.class, jp);
    }

    // ingest data dump into the catalog
    log.info("Got {} record(s) to import", dump.getItemsCount());
    ingest(dump);
  }

  public void exportData(OutputStream outputStream) throws IOException {
    // prepare data dump
    final Ise.Dump.Builder dumpBuilder = Ise.Dump.newBuilder();
    export(dumpBuilder);

    log.info("Got {} record(s) to export", dumpBuilder.getItemsCount());
    try (final JsonGenerator jg = DEFAULT_JSON_FACTORY.createGenerator(outputStream)) {
      ProtobufJacksonUtil.writeJson(dumpBuilder.build(), jg);
    }
  }

  //
  // Private
  //

  private void ingest(Ise.Dump dump) {
    final int bulkPersistRounds = (dump.getItemsCount() + BULK_PERSIST_COUNT - 1) / BULK_PERSIST_COUNT;

    for (int i = 0; i < bulkPersistRounds; ++i) {
      final int leftIndex = i * BULK_PERSIST_COUNT;
      final int rightIndex = Math.min(dump.getItemsCount(), leftIndex + BULK_PERSIST_COUNT);

      // import in bulk
      environment.executeInTransaction(tx -> {
        for (int j = leftIndex; j < rightIndex; ++j) {
          final Ise.Item item = dump.getItems(j);
          catalogDao.persist(tx, item);
        }
      });

      log.info("Imported items from {} to {}", leftIndex, rightIndex);
    }

    log.info("Import completed");
  }

  private void export(Ise.Dump.Builder dumpBuilder) {
    // TODO: split into multiple transactions
    environment.executeInTransaction(tx -> {
      final Ise.ItemQuery.Builder queryBuilder = Ise.ItemQuery.newBuilder().setLimit(20);

      for (;;) {
        final Ise.ItemQueryResult queryResult = catalogDao.getItems(tx, queryBuilder.build());
        dumpBuilder.addAllItems(queryResult.getItemsList());

        if (queryResult.getCursor().length() > 0) {
          queryBuilder.setCursor(queryResult.getCursor());
        } else {
          break; // no more items to get
        }
      }
    });
  }
}
