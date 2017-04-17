package liten.catalog.dao.exception;

import com.truward.dao.exception.DaoException;
import liten.catalog.model.Ise;
import org.jetbrains.annotations.NonNls;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Exception thrown when a duplicate alias attempted to be inserted.
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class DuplicateExternalIdException extends DaoException {
  private final Ise.ExternalId externalId;
  private final String failedItemId;
  private final String mappedItemId;

  public Ise.ExternalId getExternalId() {
    return externalId;
  }

  public String getFailedItemId() {
    return failedItemId;
  }

  public String getMappedItemId() {
    return mappedItemId;
  }

  public DuplicateExternalIdException(Ise.ExternalId externalId,
                                      @NonNls String failedItemId,
                                      @NonNls String mappedItemId) {
    super("externalId={value=" + externalId.getIdValue() + ", type=" + externalId.getIdType() + "} already " +
        "mapped to the item with id=" + mappedItemId + " and it can not be mapped to id=" + failedItemId);
    this.externalId = externalId;
    this.failedItemId = failedItemId;
    this.mappedItemId = mappedItemId;
  }
}
