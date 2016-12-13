package liten.website;

import com.truward.brikar.server.launcher.StandardLauncher;
import com.truward.demo.media.DemoMediaServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public final class LitenLauncher {

  public static void main(String[] args) throws Exception {
    try (StandardLauncher launcher = new StandardLauncher("classpath:/litenWebsite/") {
      @Override
      protected void initServlets(@Nonnull ServletContextHandler contextHandler) {
        super.initServlets(contextHandler);
        contextHandler.addServlet(DemoMediaServlet.class, "/demo/media/*");
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
