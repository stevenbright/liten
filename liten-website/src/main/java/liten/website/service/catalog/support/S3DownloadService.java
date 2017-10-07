package liten.website.service.catalog.support;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.model.Ise;
import liten.website.service.catalog.DownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * S3 bucket-based download service.
 */
@ParametersAreNonnullByDefault
public final class S3DownloadService implements DownloadService {

  private static final String FB2_FOLDER_PREFIX = "fb2-";

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final IseCatalogDao catalogDao;
  private final AmazonS3 s3Client;
  private String bucketName;
  private String bucketKeyPrefix;
  private String bucketKeySuffix;
  private long urlExpirationMillis;

  public S3DownloadService(IseCatalogDao catalogDao, AmazonS3 s3Client) {
    this.catalogDao = Objects.requireNonNull(catalogDao);
    this.s3Client = Objects.requireNonNull(s3Client);
    this.setBucketKeyPrefix("");
    this.setBucketKeySuffix("");
    this.setBucketName("");
    this.setUrlExpirationMillis(TimeUnit.HOURS.toMillis(1L));
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public void setBucketKeyPrefix(String bucketKeyPrefix) {
    this.bucketKeyPrefix = bucketKeyPrefix;
  }

  public void setBucketKeySuffix(String bucketKeySuffix) {
    this.bucketKeySuffix = bucketKeySuffix;
  }

  public void setUrlExpirationMillis(long urlExpirationMillis) {
    this.urlExpirationMillis = urlExpirationMillis;
  }

  @Override
  public String getDownloadUrl(Ise.DownloadInfo downloadInfo) {

    // Get target folder name from download information
    final String folderName = getFolderName(downloadInfo);
    if (!folderName.startsWith(FB2_FOLDER_PREFIX)) {
      log.info("S3 downloader: fallback to demo file as origin handling code is not ready yet");
      // TODO: support multiple fb2 folders
      return "/assets/sample.fb2";
    }

    final String s3Key = bucketKeyPrefix + '/' + folderName + '/' + downloadInfo.getDownloadId() + bucketKeySuffix;

    log.info("Downloading book from S3; downloadInfo={}, s3Key={}", downloadInfo, s3Key);

    // Construct S3 'gen presign url' request
    final Date expirationTime = new Date(System.currentTimeMillis() + urlExpirationMillis);
    final GeneratePresignedUrlRequest genUrlRequest = new GeneratePresignedUrlRequest(bucketName, s3Key)
        .withExpiration(expirationTime);

    // Execute presign request
    final URL presignedUrl = s3Client.generatePresignedUrl(genUrlRequest);
    final String presignedUrlStr = presignedUrl.toString();
    log.debug("Use presignedUrl={}", presignedUrlStr);

    return presignedUrlStr;
  }

  //
  // Private
  //

  private String getFolderName(Ise.DownloadInfo downloadInfo) {
    final Ise.Item origin = this.catalogDao.getEnvironment().computeInReadonlyTransaction(
        tx -> this.catalogDao.getById(tx, downloadInfo.getOriginId()));
    // TODO: factor in a language (or rather make SKU title appear somewhere inside the item)
    final Optional<Ise.Sku> originEnSku = origin.getSkusList().stream()
        .filter(x -> x.getLanguage().equals("en"))
        .findAny();
    if (!originEnSku.isPresent()) {
      throw new IllegalStateException("Missing origin SKU for downloadInfo=" + downloadInfo);
    }

    return originEnSku.get().getTitle();
  }
}
