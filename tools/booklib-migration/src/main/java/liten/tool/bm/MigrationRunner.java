package liten.tool.bm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MigrationRunner implements Runnable {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  public void run() {
    log.info("Started...");
  }
}
