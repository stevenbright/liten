package com.truward.xodus.migration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.truward.kvdao.xodus.XodusMetadata;
import com.truward.kvdao.xodus.metadata.MetadataDao;
import com.truward.xodus.migration.exception.MigrationException;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Utility class for running Xodus database migration.
 *
 * Sample use:
 * <code>
 *  final MigrationExecutor executor = MigrationExecutor.newBuilder()
 *      .add("1.0", (context) -> { "* migration logic for version 1.0 *" })
 *      .add("1.1", (context) -> { "* migration logic for version 1.1 *" })
 *      .add("2.0", (context) -> { "* migration logic for version 2.0 *" })
 *      .addLast("3.0") // this should be the last call of version, that should be present in the datastore
 *      .setInitialVersion("1.0") // setup initial version, so it will be initialized if it has not been set
 *      .setSourcePath(new File("/path/to/source/database")) // where original database is located
 *      .setTargetPath(new File("/path/to/target/database")) // where target database should be put
 *      .build();
 *
 *  // run migration
 *  if (!executor.migrate()) {
 *    // source version already contains latest version, migration is not needed, so target directory has
 *    // not been created
 *  } else {
 *    // ... migration completed successfully, target directory contains latest database
 *  }
 * </code>
 */
@ParametersAreNonnullByDefault
public class MigrationExecutor {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final List<String> versionChain;
  private final Map<String, MigrationCallback> migrationCallbackMap;
  private final File sourceDirectory;
  private final File targetDirectory;
  private final Function<Environment, String> versionReader;
  private final VersionUpdateStrategy versionUpdateStrategy;

  private MigrationExecutor(
      File sourceDirectory,
      File targetDirectory,
      List<String> versionChain,
      Map<String, MigrationCallback> migrationCallbackMap,
      Function<Environment, String> versionReader,
      VersionUpdateStrategy versionUpdateStrategy) {
    if (versionChain.isEmpty()) {
      throw new IllegalStateException("versionChain is empty"); // should never happen
    }

    if (requireNonNull(versionUpdateStrategy) == VersionUpdateStrategy.NOT_SET) {
      throw new IllegalArgumentException("versionUpdateStrategy"); // should never happen
    }

    this.versionChain = ImmutableList.copyOf(versionChain);
    this.migrationCallbackMap = ImmutableMap.copyOf(migrationCallbackMap);
    this.versionReader = requireNonNull(versionReader);
    this.sourceDirectory = requireNonNull(sourceDirectory, "sourceDirectory");
    this.targetDirectory = requireNonNull(targetDirectory, "targetDirectory");
    this.versionUpdateStrategy = versionUpdateStrategy;
  }

  /**
   * Executes migration
   *
   * @return True, if migration completed successfully, false, if migration canceled due to source database already
   *         having the latest version of data.
   * @throws MigrationException On migration error
   */
  public boolean migrate() throws MigrationException {
    log.info("Migration started");

    if (!sourceDirectory.isDirectory() || !sourceDirectory.exists()) {
      throw new MigrationException("Source directory=" + sourceDirectory.getAbsolutePath() + " does not exist");
    }

    if (!targetDirectory.mkdir()) {
      if (targetDirectory.exists()) {
        final File[] targetDirectoryFiles = targetDirectory.listFiles();
        if (targetDirectoryFiles != null && targetDirectoryFiles.length > 0) {
          throw new MigrationException("Target directory=" + targetDirectory.getAbsolutePath() +
              " exists and it is not empty");
        }
      } else {
        throw new MigrationException("Can't create target directory=" + targetDirectory.getAbsolutePath() +
            " it either exists or can't be created");
      }
    }

    try {
      final boolean result = executeMigration();
      log.info("Migration {}", result ? "completed" : "canceled due to source dir matching target dir");
      return result;
    } catch (IOException e) {
      throw new MigrationException(e);
    }
  }

  private boolean executeMigration() throws IOException {
    final int lastVersionIndex = this.versionChain.size() - 1;
    for (File fromPath = sourceDirectory;;) {
      Environment fromEnvironment = Environments.newInstance(fromPath);
      try {
        String version = this.versionReader.apply(fromEnvironment);
        if (version.equals(this.versionChain.get(lastVersionIndex))) {

          if (fromPath == sourceDirectory) {
            // special case: source environment matches target
            return false;
          }

          // flush all the transactions
          fromEnvironment.close();
          fromEnvironment = null;

          // move files from intermediate location to the target directory
          FileSystemUtils.copyRecursively(fromPath, targetDirectory);
          FileSystemUtils.deleteRecursively(fromPath);
          break;
        }

        final File toPath = Files.createTempDirectory("xodus-migration").toFile();

        final MigrationCallback migrationCallback = this.migrationCallbackMap.get(version);
        if (migrationCallback == null) {
          throw new MigrationException("Migration callback is not set for version=" + version);
        }


        final int versionIndex = this.versionChain.indexOf(version);
        if (versionIndex < 0 || versionIndex == lastVersionIndex) {
          throw new MigrationException("Invalid migration version index=" + versionIndex + " for version=" + version);
        }
        final String nextVersion = this.versionChain.get(versionIndex + 1);

        // execute migration itself
        Environment toEnvironment = Environments.newInstance(toPath);
        try {
          final Environment srcEnv = fromEnvironment;
          if (versionUpdateStrategy == VersionUpdateStrategy.DEFAULT) {
            // copy metadata
            final Store fromMetadataStore = fromEnvironment.computeInTransaction(tx ->
                srcEnv.openStore(XodusMetadata.METADATA_STORE_NAME, StoreConfig.WITHOUT_DUPLICATES, tx));
            final Store toMetadataStore = toEnvironment.computeInTransaction(tx ->
              toEnvironment.openStore(XodusMetadata.METADATA_STORE_NAME, StoreConfig.WITHOUT_DUPLICATES, tx));
            MigrationUtil.copyStores(fromMetadataStore, toMetadataStore);
          }

          migrationCallback.migrate(new MigrationContext(
              fromEnvironment,
              version,
              toEnvironment,
              nextVersion));

          if (versionUpdateStrategy == VersionUpdateStrategy.DEFAULT) {
            // update version in the metadata table
            MetadataDao metadataDao = new MetadataDao(toEnvironment);
            toEnvironment.executeInTransaction(tx -> {
              final Optional<String> oldVersion = metadataDao.getVersion(tx);
              if (oldVersion.isPresent()) {
                metadataDao.update(tx, oldVersion.get(), nextVersion);
              } else {
                metadataDao.create(tx, nextVersion);
              }
            });
          }

          // regardless who updated version in the metadata store, make sure that to-environment contains right
          // version afterwards
          final String newVersion = this.versionReader.apply(toEnvironment);
          if (!nextVersion.equals(newVersion)) {
            throw new MigrationException("Migration failure: updated data store does not contain expected " +
                "version=" + nextVersion + ", instead it has version=" + newVersion);
          }
        } finally {
          toEnvironment.close();
        }

        log.info("Migrated from version={} to version={}", version, nextVersion);

        if (fromPath != sourceDirectory) {
          // close environment first to flush transactions
          fromEnvironment.close();
          fromEnvironment = null;

          // we no longer need intermediate versions and last migration had not been run yet, so
          // delete it and work with the just migrated version
          FileSystemUtils.deleteRecursively(fromPath);
        }

        fromPath = toPath;
      } finally {
        if (fromEnvironment != null) {
          fromEnvironment.close();
        }
      }
    }

    return true;
  }

  /**
   * @return New instance of builder for this class
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  public enum VersionUpdateStrategy {
    /**
     * Initial state of version updater. This tells migration executor to use {@link #DEFAULT} if possible.
     */
    NOT_SET,

    /** Tells migration executor to rely on migration callback to put proper version */
    CALLBACK,

    /**
     * Tells migration executor to do version updates automatically.
     * This option is mutually exclusive with {@link #CALLBACK}. Also it can't be used if
     * version is stored in non-metadata store, see also {@link MetadataDao}.
     */
    DEFAULT
  }

  /**
   * Builder for the parent class.
   */
  public static final class Builder {
    private final List<String> versionChain = new ArrayList<>();
    private boolean lastVersionAdded = false;
    private final Map<String, MigrationCallback> migrationCallbackMap = new HashMap<>();
    private File sourceDirectory;
    private File targetDirectory;
    private Function<Environment, String> versionReader;
    private Function<Environment, String> initialVersionProvider = sourceEnv -> {
      throw new IllegalStateException("Cannot read version from the store");
    };
    private VersionUpdateStrategy versionUpdateStrategy = VersionUpdateStrategy.NOT_SET;

    private Builder() {}

    public Builder add(String version, MigrationCallback callback) {
      addChainedVersion(version);
      this.migrationCallbackMap.put(version, callback);
      return this;
    }

    public Builder addLast(String version) {
      addChainedVersion(version);
      this.lastVersionAdded = true;
      return this;
    }

    public Builder setSourceDirectory(File sourceDirectory) {
      this.sourceDirectory = requireNonNull(sourceDirectory);
      return this;
    }

    public Builder setTargetDirectory(File targetDirectory) {
      this.targetDirectory = requireNonNull(targetDirectory);
      return this;
    }

    public Builder setVersionReader(Function<Environment, String> versionReader) {
      if (this.versionUpdateStrategy == VersionUpdateStrategy.DEFAULT) {
        throw new IllegalStateException("Custom version reader can not be used with " + this.versionUpdateStrategy);
      }

      this.versionReader = requireNonNull(versionReader);
      this.versionUpdateStrategy = VersionUpdateStrategy.CALLBACK;
      return this;
    }

    public Builder setInitialVersionProvider(Function<Environment, String> initialVersionProvider) {
      this.initialVersionProvider = requireNonNull(initialVersionProvider);
      return this;
    }

    public Builder setInitialVersion(String initialVersion) {
      return this.setInitialVersionProvider(e -> initialVersion);
    }

    public Builder setVersionUpdateStrategy(VersionUpdateStrategy versionUpdateStrategy) {
      if (this.versionUpdateStrategy == VersionUpdateStrategy.NOT_SET ||
          this.versionUpdateStrategy == versionUpdateStrategy) {
        this.versionUpdateStrategy = versionUpdateStrategy;
        return this;
      }

      throw new IllegalStateException("Version update strategy can not be changed once it is set " +
          "explicitly or implicitly. Stored strategy=" + this.versionUpdateStrategy +
          ", suggested strategy=" + versionUpdateStrategy);
    }

    public MigrationExecutor build() {
      // if version update strategy has not been set, it means we can resort to default one as we use known
      // method of reading and updating database version based on metadata table
      if (versionUpdateStrategy == VersionUpdateStrategy.NOT_SET) {
        this.versionUpdateStrategy = VersionUpdateStrategy.DEFAULT;
      }

      return new MigrationExecutor(
          sourceDirectory,
          targetDirectory,
          versionChain,
          migrationCallbackMap,
          getVersionReader(),
          versionUpdateStrategy);
    }

    //
    // Private (Builder)
    //

    private void addChainedVersion(String version) {
      if (lastVersionAdded) {
        throw new IllegalStateException("Last version has already been added");
      }

      if (versionChain.contains(version)) {
        throw new IllegalArgumentException("Version=" + version + " already exists");
      }

      final int last = versionChain.size() - 1;
      if (last >= 0 && version.compareTo(versionChain.get(last)) <= 0) {
        throw new IllegalArgumentException("Version=" + version + " should be lexicographically greater than " +
            "previous version=" + versionChain.get(last));
      }

      this.versionChain.add(version);
    }

    private Function<Environment, String> getVersionReader() {
      Function<Environment, String> versionReader = this.versionReader;
      if (versionReader == null) {
        final Function<Environment, String> initialVersionProvider = this.initialVersionProvider;
        versionReader = sourceEnv -> {
          final MetadataDao metadataDao = new MetadataDao(sourceEnv);
          final Optional<String> version = sourceEnv.computeInTransaction(metadataDao::getVersion);
          return version.orElseGet(() -> initialVersionProvider.apply(sourceEnv));
        };
      }
      return versionReader;
    }
  } // class Builder
}
