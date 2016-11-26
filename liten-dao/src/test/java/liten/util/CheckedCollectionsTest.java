package liten.util;

import org.junit.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests {@link CheckedCollections}.
 */
public final class CheckedCollectionsTest {
  @Test
  public void shouldCopyEmptyCollections() {
    assertEquals(emptyList(), CheckedCollections.copyList(emptyList(), "list"));
    assertEquals(emptySet(), CheckedCollections.copySet(emptyList(), "set"));
  }

  @Test
  public void shouldCopySingleItemCollections() {
    assertEquals(singletonList(1), CheckedCollections.copyList(singletonList(1), "list"));
    assertEquals(singleton(1), CheckedCollections.copySet(singletonList(1), "set1"));
    assertEquals(singleton(1), CheckedCollections.copySet(asList(1, 1, 1), "set2"));
    assertEquals(singleton(1), CheckedCollections.copySet(new HashSet<>(singletonList(1)), "set3"));
  }

  @Test
  public void shouldCopyMultiItemCollections() {
    assertEquals(asList(1, 2, 3), CheckedCollections.copyList(asList(1, 2, 3), "list"));
    assertEquals(new HashSet<>(asList(1, 2, 3)), CheckedCollections.copySet(asList(1, 2, 3), "set"));
  }

  @Test
  public void shouldDisallowNullsInList() {
    try {
      final List<Long> list = new ArrayList<>();
      list.add(null);
      CheckedCollections.copyList(list, "list");
      fail();
    } catch (NullPointerException e) {
      assertEquals("list[0]", e.getMessage());
    }

    try {
      CheckedCollections.copyList(asList(null, 1), "list");
      fail();
    } catch (NullPointerException e) {
      assertEquals("list[0]", e.getMessage());
    }

    try {
      CheckedCollections.copyList(asList(1, null), "list");
      fail();
    } catch (NullPointerException e) {
      assertEquals("list[1]", e.getMessage());
    }
  }

  @Test
  public void shouldDisallowNullsInSet() {
    try {
      final List<Long> list = new ArrayList<>();
      list.add(null);
      CheckedCollections.copySet(list, "set");
      fail();
    } catch (NullPointerException e) {
      assertEquals("set[0]", e.getMessage());
    }

    try {
      CheckedCollections.copySet(asList(null, 1), "set");
      fail();
    } catch (NullPointerException e) {
      assertEquals("set[0]", e.getMessage());
    }

    try {
      CheckedCollections.copySet(asList(1, null), "set");
      fail();
    } catch (NullPointerException e) {
      assertEquals("set[1]", e.getMessage());
    }
  }

  @Test
  public void shouldCopyEmptyMap() {
    assertEquals(Collections.emptyMap(), CheckedCollections.copyMap(Collections.emptyMap(), "emptyMap"));
  }

  @Test
  public void shouldCopySingleElementMap() {
    final String k = "k";
    final Integer v = 1;
    assertEquals(Collections.singletonMap(k, v),
        CheckedCollections.copyMap(Collections.singletonMap(k, v), "singleElemMap"));
  }

  @Test
  public void shouldCopyMultiElementMap() {
    final Map<String, Integer> map = new HashMap<>();
    map.put("a", 1);
    map.put("b", 2);
    assertEquals(map, CheckedCollections.copyMap(map, "multiElemMap"));
  }

  @Test
  public void shouldDisallowNullsInMap() {
    try {
      CheckedCollections.copyMap(Collections.singletonMap("a", null), "map");
      fail();
    } catch (NullPointerException e) {
      assertEquals("map[0]", e.getMessage());
    }

    try {
      CheckedCollections.copyMap(Collections.singletonMap(null, "1"), "map");
      fail();
    } catch (NullPointerException e) {
      assertEquals("map[0]", e.getMessage());
    }

    try {
      final Map<String, Integer> map = new HashMap<>();
      map.put("a", 1);
      map.put("b", null);
      CheckedCollections.copyMap(map, "map");
      fail();
    } catch (NullPointerException e) {
      assertEquals("map[1]", e.getMessage());
    }
  }
}
