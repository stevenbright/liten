package liten.website.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.truward.semantic.id.exception.IdParsingException;
import liten.catalog.model.Ise;
import liten.website.exception.ResourceNotFoundException;
import liten.website.model.catalog.CatalogEntry;
import liten.website.service.catalog.DownloadService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * Responds to download requests.
 */
@Controller
@RequestMapping("/g/download")
@ParametersAreNonnullByDefault
public final class DownloadController extends BaseHtmlController {
  private final DownloadService downloadService;

  public DownloadController(DownloadService downloadService) {
    this.downloadService = Objects.requireNonNull(downloadService);
  }

  @RequestMapping("item/{downloadParameters}")
  public String getDownloadUrl(
      @PathVariable("downloadParameters") String parameters) {
    // TODO: redirect to demo URL
    final Ise.DownloadInfo downloadInfo;
    try {
      downloadInfo = Ise.DownloadInfo.parseFrom(CatalogEntry.DOWNLOAD_PARAMETERS_CODEC.decodeBytes(parameters));
    } catch (IdParsingException | InvalidProtocolBufferException e) {
      throw new ResourceNotFoundException(e);
    }

    return "redirect:" + downloadService.getDownloadUrl(downloadInfo);
  }
}