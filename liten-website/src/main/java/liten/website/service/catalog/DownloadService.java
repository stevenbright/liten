package liten.website.service.catalog;

import liten.catalog.model.Ise;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An abstraction over download service.
 */
@ParametersAreNonnullByDefault
public interface DownloadService {

  String getDownloadUrl(Ise.DownloadInfo downloadInfo);
}
