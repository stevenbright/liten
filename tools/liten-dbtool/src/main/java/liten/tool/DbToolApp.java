package liten.tool;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * DB Tool Application.
 */
public final class DbToolApp {

  public static void main(String[] args) throws Exception {
    final String[] contextPaths = {"classpath:/litenDbToolApp/spring/app-context.xml"};

    try (final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(contextPaths, false)) {
      addEnvironmentProperties(context);
      context.refresh(); // explicitly refresh the context
      context.start();

      // get Runnable application bean and run it
      context.getBean("tool.app", Runnable.class).run();
    }
  }

  //
  // Private
  //

  private static void addEnvironmentProperties(ConfigurableApplicationContext context) throws IOException {
    final List<Resource> resources = new ArrayList<>(2);
    resources.add(context.getResource("classpath:/litenDbToolApp/core.properties"));

    // do we have property override? if yes - add it
    final String propsOverridePath = System.getProperty("app.properties.override");
    if (propsOverridePath != null) {
      resources.add(context.getResource(propsOverridePath));
    }

    // fill properties
    final Properties properties = new Properties();
    for (final Resource resource : resources) {
      if (!resource.exists()) {
        throw new IOException("Resource " + resource.getFilename() + " does not exist");
      }

      PropertiesLoaderUtils.fillProperties(properties, resource);
    }

    // create property source and insert it into a context
    final PropertiesPropertySource propertySource = new PropertiesPropertySource("app", properties);
    context.getEnvironment().getPropertySources().addFirst(propertySource);
  }
}
