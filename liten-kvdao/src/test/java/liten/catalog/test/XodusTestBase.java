package liten.catalog.test;

import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Alexander Shabanov
 */
public abstract class XodusTestBase {
  private Path tempDir;
  protected Environment environment;

  @Before
  public void initEnvironment() throws IOException {
    if (environment != null) {
      return;
    }

    tempDir = Files.createTempDirectory("CatalogDaoTest");
    final String dirPath = tempDir.toFile().getAbsolutePath();

    environment = Environments.newInstance(dirPath);
  }

  @After
  public void disposeEnvironment() throws IOException {
    if (environment == null) {
      return;
    }

    try {
      environment.close();
    } finally {
      environment = null;
    }

    //Files.delete(tempDir);
  }
}
