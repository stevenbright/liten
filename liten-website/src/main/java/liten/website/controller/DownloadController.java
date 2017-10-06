package liten.website.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.truward.semantic.id.exception.IdParsingException;
import liten.catalog.model.Ise;
import liten.website.exception.ResourceNotFoundException;
import liten.website.model.catalog.CatalogEntry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Date;

/**
 * Responds to download requests.
 */
@Controller
@RequestMapping("/g/download")
@ParametersAreNonnullByDefault
public final class DownloadController extends BaseHtmlController {

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

    if (downloadInfo.getDownloadId().equals("349760")) {
      // available through http://127.0.0.1:8080/g/cat/item/ci1-000jbzg1
      return "redirect:/assets/sample.fb2";
    }

    final String id = "x-" + downloadInfo.getDownloadType() + "-" + downloadInfo.getDownloadId();
    return "redirect:/g/download/demo/text/" + id;
  }

  @RequestMapping("/demo/text/{id}")
  @ResponseBody
  public ResponseEntity<String> demoText(@PathVariable("id") String id) {
    @SuppressWarnings("StringBufferReplaceableByString") final StringBuilder testContent = new StringBuilder(100);
    testContent.append("Test File\n");
    testContent.append("ID: ").append(id).append('\n');
    testContent.append("Generated at ").append(new Date()).append('\n');
    testContent.append('\n')
        .append("Lorem ipsum dolorem sit amet...\n");
    return new ResponseEntity<>(testContent.toString(), HttpStatus.OK);
  }
}