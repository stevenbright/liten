package liten.website;

import com.truward.brikar.server.launcher.StandardLauncher;
import com.truward.demo.media.DemoMediaServlet;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Alexander Shabanov
 */
public final class LitenLauncher {
  private static final String SESSION_DIR_PROP_NAME = "liten.web.sessionDir";

  public static void main(String[] args) throws Exception {
    try (StandardLauncher launcher = new StandardLauncher("classpath:/litenWebsite/") {
      @Override
      protected void initServlets(@Nonnull ServletContextHandler contextHandler) {
        super.initServlets(contextHandler);
        contextHandler.addServlet(DemoMediaServlet.class, "/demo/media/*");
      }

      @Override
      protected void initContextFilters(@Nonnull ServletContextHandler contextHandler) {
        super.initContextFilters(contextHandler);

        // persist sessions on the disk to avoid noisy reload problem
        final HashSessionManager sessionManager = new HashSessionManager();
        try {
          final String sessionDirPath = this.getPropertyResolver().getProperty(SESSION_DIR_PROP_NAME);
          final File sessionDir = new File(Objects.requireNonNull(sessionDirPath, SESSION_DIR_PROP_NAME));
          if (!sessionDir.exists() && !sessionDir.mkdir()) {
            throw new IOException("Unable to create session dir");
          }

          sessionManager.saveSessions(true);
          sessionManager.setStoreDirectory(sessionDir);
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
        final SessionHandler sessionHandler = new SessionHandler(sessionManager);
        contextHandler.setSessionHandler(sessionHandler);
      }
    }) {
      launcher
          .setSpringSecurityEnabled(true)
          .setSessionsEnabled(true)
          .setStaticHandlerEnabled(true)
          .start();
    }
  }
}
