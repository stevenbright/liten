package liten.tool.bm;

import liten.tool.bm.transfer.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MigrationRunner implements Runnable {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final TransferService transferService;

  public MigrationRunner(TransferService transferService) {
    this.transferService = transferService;
  }

  @Override
  public void run() {
    log.info("About to run migration...");

    if (!transferService.prepare()) {
      return;
    }

    for (String startId = null;;) {
      log.info("Moving next chunk of data");
      startId = transferService.transferNext(startId);
      if (startId == null) {
        break;
      }
    }

    transferService.complete();

    log.info("Migration Completed.");
  }
}
