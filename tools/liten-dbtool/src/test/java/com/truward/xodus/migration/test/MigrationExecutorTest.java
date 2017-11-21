package com.truward.xodus.migration.test;

import com.truward.kvdao.xodus.XodusMetadata;
import com.truward.kvdao.xodus.metadata.MetadataDao;
import com.truward.xodus.migration.MigrationExecutor;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static jetbrains.exodus.bindings.StringBinding.entryToString;
import static jetbrains.exodus.bindings.StringBinding.stringToEntry;
import static org.junit.Assert.*;

public final class MigrationExecutorTest {
  private final List<File> tempDirectories = new CopyOnWriteArrayList<>();
  private File sourceDirectory;
  private File targetDirectory;

  @Before
  public void init() throws IOException {
    sourceDirectory = createTempDirectory("source");
    targetDirectory = createTempDirectory("target");
  }

  @After
  public void cleanup() {
    for (final File dir : tempDirectories) {
      FileSystemUtils.deleteRecursively(dir);
    }
  }

  @Test
  public void shouldNotMigrateSingleVersionStore() {
    // Given:
    final String initialVersion = "1.0";

    final MigrationExecutor executor = MigrationExecutor.newBuilder()
        .addLast(initialVersion)
        .setSourceDirectory(sourceDirectory)
        .setTargetDirectory(targetDirectory)
        .setInitialVersionProvider(e -> {
          throw new AssertionError("There should be no attempt to read initial version");
        })
        .build();

    openAndExecute(sourceDirectory, tx -> {
      MetadataDao metadataDao = new MetadataDao(tx.getEnvironment());
      metadataDao.create(tx, initialVersion);
    });

    // Check, before executing migration
    openAndRun(sourceDirectory, e -> XodusMetadata.verifyVersion(e, initialVersion));

    // When:
    final boolean migrated = executor.migrate();

    // Then:
    assertFalse("Migration shall not be done for single version DB", migrated);

    File[] targetFiles = targetDirectory.listFiles();
    if (targetFiles != null) {
      assertArrayEquals("Target directory shall be empty", new File[0], targetFiles);
    }
  }

  @Test(timeout = 20000L/* 20 sec */)
  public void shouldMigrateToLastVersion() {
    // Given:
    final String ver10 = "1.0";
    final String ver11 = "1.1";
    final String ver20 = "2.0";
    final String ver30 = "3.0";

    final MigrationExecutor executor = MigrationExecutor.newBuilder()
        .add(ver10, ctx -> {})
        .add(ver11, ctx -> {})
        .add(ver20, ctx -> {})
        .addLast(ver30)
        .setSourceDirectory(sourceDirectory)
        .setTargetDirectory(targetDirectory)
        .setInitialVersion(ver10)
        .build();

    // When:
    final boolean migrated = executor.migrate();

    // Then:
    assertTrue(migrated);

    openAndExecute(targetDirectory, tx -> {
      MetadataDao metadataDao = new MetadataDao(tx.getEnvironment());
      final Optional<String> version = metadataDao.getVersion(tx);
      assertEquals(Optional.of(ver30), version);
    });
  }

  @Test
  public void shouldUseCustomVersionDao() {
    // Given:
    final String ver10 = "1.0";
    final String ver20 = "2.0";

    // Custom version store parameters
    final String customVersions = "custom-versions";
    final ByteIterable currentVersionKey = stringToEntry("current");

    final MigrationExecutor executor = MigrationExecutor.newBuilder()
        .add(ver10, ctx -> {
          final Environment e = ctx.getTargetEnvironment();
          e.executeInTransaction(tx -> {
            final Store versions = e.openStore(customVersions, StoreConfig.WITHOUT_DUPLICATES, tx);
            versions.put(tx, currentVersionKey, stringToEntry(ver20));
          });
        })
        .addLast(ver20)
        .setSourceDirectory(sourceDirectory)
        .setTargetDirectory(targetDirectory)
        .setVersionUpdateStrategy(MigrationExecutor.VersionUpdateStrategy.CALLBACK)
        .setVersionReader(e -> e.computeInTransaction(tx -> {
          final Store versions = e.openStore(customVersions, StoreConfig.WITHOUT_DUPLICATES, tx);
          final ByteIterable ver = versions.get(tx, currentVersionKey);
          if (ver == null) {
            return ver10;
          }
          return entryToString(ver);
        }))
        .build();

    // When:
    final boolean migrated = executor.migrate();

    // Then:
    assertTrue(migrated);

    openAndExecute(targetDirectory, tx -> {
      final Environment e = tx.getEnvironment();
      final Store versions = e.openStore(customVersions, StoreConfig.WITHOUT_DUPLICATES, tx);
      final ByteIterable ver = versions.get(tx, currentVersionKey);
      assertNotNull("Latest version is missing", ver);
      assertEquals("Latest version mismatch", ver20, entryToString(ver));
    });
  }

  //
  // Private
  //

  private File createTempDirectory(String role) throws IOException {
    final File tempDirectory = Files.createTempDirectory("migration-executor-test-env-" + role + "-").toFile();
    this.tempDirectories.add(tempDirectory);
    return tempDirectory;
  }

  private static void openAndRun(File sourceDirectory, Consumer<Environment> callback) {
    final Environment environment = Environments.newInstance(sourceDirectory);
    try {
      callback.accept(environment);
    } finally {
      environment.close();
    }
  }

  private static void openAndExecute(File sourceDirectory, TransactionalExecutable callback) {
    openAndRun(sourceDirectory, (e) -> e.executeInTransaction(callback));
  }
}
