package liten.catalog.test;

import jetbrains.exodus.env.Store;
import org.junit.Test;

import static jetbrains.exodus.bindings.StringBinding.entryToString;
import static jetbrains.exodus.bindings.StringBinding.stringToEntry;
import static jetbrains.exodus.env.StoreConfig.WITHOUT_DUPLICATES;
import static org.junit.Assert.assertEquals;

/**
 * @author Alexander Shabanov
 */
public final class IseCatalogDaoTest extends XodusTestBase {

  @Test
  public void shouldCreateDb() {
    final Store store = environment.computeInTransaction(txn ->
        environment.openStore("Messages", WITHOUT_DUPLICATES, txn));

    environment.executeInTransaction((txn) ->
    { store.put(txn, stringToEntry("Hello"),
        stringToEntry("World!")); });

    final String str = environment.computeInTransaction(txn ->
        entryToString(store.get(txn, stringToEntry("Hello"))));
    assertEquals("World!", str);
  }
}
