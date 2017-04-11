package liten.catalog.dao.support;

import com.truward.semantic.id.IdCodec;
import com.truward.semantic.id.SemanticIdCodec;
import liten.catalog.dao.CatalogDao;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class DefaultCatalogDao implements CatalogDao {
  private static final IdCodec ICE_ITEM_CODEC = SemanticIdCodec.forPrefixNames("C1");

  @Override
  public Object getById(String id) {
    return null;
  }

  @Override
  public List<String> getNameHints(String prefix) {
    return null;
  }
}
