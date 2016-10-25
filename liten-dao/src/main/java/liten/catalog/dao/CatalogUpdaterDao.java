package liten.catalog.dao;

import liten.catalog.dao.model.IceItem;

import java.util.List;

/**
 * @author Alexander Shabanov
 */
public interface CatalogUpdaterDao {

  default List<Long> persist(List<IceItem> item) {
    throw new UnsupportedOperationException();
  }
}
