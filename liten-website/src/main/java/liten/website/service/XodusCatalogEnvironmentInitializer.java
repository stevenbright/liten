package liten.website.service;

import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;
import org.springframework.util.FileSystemUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Local initializer for Xodus DAO.
 */
public final class XodusCatalogEnvironmentInitializer implements Closeable {

  private Path tempDir;
  private Environment environment;

  public XodusCatalogEnvironmentInitializer() throws IOException {
    // TODO: split prod vs dev mode
    tempDir = Files.createTempDirectory("liten-website-dev-db");
    final String dirPath = tempDir.toFile().getAbsolutePath();

    environment = Environments.newInstance(dirPath);
  }

  public void close() {
    if (environment != null) {
      try {
        environment.close();
      } finally {
        environment = null;
      }

      FileSystemUtils.deleteRecursively(tempDir.toFile());
    }

    environment = null;
  }

  @SuppressWarnings("unused") // used in spring config
  public Environment getEnvironment() {
    if (environment == null) {
      throw new IllegalStateException();
    }
    return environment;
  }
}
