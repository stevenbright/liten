package liten.website.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * @author Alexander Shabanov
 */
public class DemoInitializer {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @PostConstruct
  public void init() {
    log.info("Initializing demo data");
  }
}
