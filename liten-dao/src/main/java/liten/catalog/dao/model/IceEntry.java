package liten.catalog.dao.model;

import java.util.List;

/**
 * @author Alexander Shabanov
 */
public final class IceEntry {
  private IceItem item;
  private List<Sku> skus;

  public static final class Sku {
    private IceSku sku;
    private List<IceInstance> instances;
  }
}
