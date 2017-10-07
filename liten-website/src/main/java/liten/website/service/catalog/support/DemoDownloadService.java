package liten.website.service.catalog.support;

import liten.catalog.model.Ise;
import liten.website.service.catalog.DownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Demo download service.
 */
@ParametersAreNonnullByDefault
public final class DemoDownloadService implements DownloadService {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  public String getDownloadUrl(Ise.DownloadInfo downloadInfo) {
    log.info("Demo download, downloadInfo={}", downloadInfo);
    return "/assets/sample.fb2?download-id=" + downloadInfo.getDownloadId();
  }
}
