package liten.test.integration.website;

import com.truward.brikar.maintenance.LaunchUtil;
import com.truward.brikar.maintenance.ServerApiUtil;
import com.truward.brikar.server.auth.SimpleServiceUser;
import com.truward.brikar.server.launcher.StandardLauncher;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * @author Alexander Shabanov
 */
public abstract class AbstractIntegrationTestBase {

  private static final Logger LOG;

  static {
    // configure loggers - use default logger file
    System.setProperty("app.logback.rootLogId", "STDOUT");
    System.setProperty("app.logback.requestLogLevel", "TRACE");
    System.setProperty("logback.configurationFile", "default-service-logback.xml");
    LOG = LoggerFactory.getLogger("IntegrationTest");
  }

  private static Thread THREAD;
  private static int PORT_NUMBER = LaunchUtil.getAvailablePort();
  private static Server SERVER;

  @AfterClass
  public static void stopServer() {
    if (SERVER != null) {
      try {
        SERVER.stop();
      } catch (Exception ignored) {
        // do nothing - failure to stop server might be caused by not being able to start it
      }

      SERVER = null;
    }

    try {
      THREAD.join();
    } catch (InterruptedException e) {
      Thread.interrupted();
    }

    LOG.info("Server stopped");
  }

  protected static void initServer(@Nonnull String springConfig) {
    THREAD = new Thread(() -> {
      try {
        final String props = StandardLauncher.CONFIG_KEY_PORT + "=" + PORT_NUMBER + "\n" +
            StandardLauncher.CONFIG_KEY_SHUTDOWN_DELAY + "=100\n" +
            "\n";

        final File tmpFile = File.createTempFile("litenIntegrationTest", ".properties");
        tmpFile.deleteOnExit();
        Files.write(Paths.get(tmpFile.toURI()), props.getBytes(StandardCharsets.UTF_8));

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    THREAD.start();
    LOG.info("Server started");

//    ServerApiUtil.waitUntilStarted(user, getServerUrl("/rest"));
    LOG.info("Server initialized");
  }
}
