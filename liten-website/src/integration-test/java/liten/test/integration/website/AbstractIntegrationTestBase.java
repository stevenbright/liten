package liten.test.integration.website;

import com.truward.brikar.maintenance.LaunchUtil;
import com.truward.brikar.maintenance.ServerApiUtil;
import com.truward.brikar.server.auth.SimpleServiceUser;
import com.truward.brikar.server.launcher.StandardLauncher;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertySource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

  protected static void initServer(@Nullable SimpleServiceUser user, @Nonnull String springConfig) {
    StandardLauncher.ensureLoggersConfigured();

    THREAD = new Thread(() -> {
      // TODO: replace with TempConfiguration
      try {
        final String props = StandardLauncher.CONFIG_KEY_PORT + "=" + PORT_NUMBER + "\n" +
            StandardLauncher.CONFIG_KEY_SHUTDOWN_DELAY + "=100\n" +
            "\n";

        final File tmpFile = File.createTempFile("brikar-website-integration-test", ".properties");
        tmpFile.deleteOnExit();
        Files.write(Paths.get(tmpFile.toURI()), props.getBytes(StandardCharsets.UTF_8));

        System.setProperty(StandardLauncher.SYS_PROP_SETTINGS_OVERRIDE, tmpFile.toURI().toURL().toExternalForm());

        // start server
        try (final StandardLauncher launcher = new StandardLauncher(springConfig) {
          @Override
          protected void setServerSettings(@Nonnull Server server) {
            super.setServerSettings(server);
            SERVER = server;
          }
        }) {
          launcher.start();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    THREAD.start();
    LOG.info("Server started");

    ServerApiUtil.waitUntilStarted(user, getServerUrl("/rest"));
    LOG.info("Server initialized");
  }

  @Nonnull
  protected static URI getServerUrl(String relPath) {
    return URI.create("http://127.0.0.1:" + PORT_NUMBER + relPath);
  }
}
