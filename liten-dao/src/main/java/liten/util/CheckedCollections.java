package liten.util;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * DAO collections utilities.
 */
@ParametersAreNonnullByDefault
public final class CheckedCollections {
  private CheckedCollections() {}

  public static <K, V> Map<K, V> copyMap(@Nullable Map<K, V> map, String parameterName) {
    Objects.requireNonNull(map, parameterName);
    switch (map.size()) {
      case 0:
        return Collections.emptyMap();

      case 1:
        final Map.Entry<K, V> entry = map.entrySet().iterator().next();
        return Collections.singletonMap(
            requireNonNullElement(entry.getKey(), 0, parameterName),
            requireNonNullElement(entry.getValue(), 0, parameterName));

      default:
        final Map<K, V> copy = new HashMap<>(map.size() * 2);
        int pos = 0;

        for (final Map.Entry<K, V> e : map.entrySet()) {
          copy.put(requireNonNullElement(e.getKey(), pos, parameterName),
              requireNonNullElement(e.getValue(), pos, parameterName));
          ++pos;
        }

        return Collections.unmodifiableMap(copy);
    }
  }

  public static <T> List<T> copyList(@Nullable Collection<T> elements, String parameterName) {
    Objects.requireNonNull(elements, parameterName);

    switch (elements.size()) {
      case 0:
        return Collections.emptyList();

      case 1:
        return Collections.singletonList(requireNonNullElement(elements.iterator().next(), 0, parameterName));

      default:
        // create checked copy
        final List<T> copy = new ArrayList<>(elements.size());
        checkedCopy(elements, copy, parameterName);
        return Collections.unmodifiableList(copy);
    }
  }

  public static <T> Set<T> copySet(@Nullable Collection<T> elements, String parameterName) {
    Objects.requireNonNull(elements, parameterName);

    switch (elements.size()) {
      case 0:
        return Collections.emptySet();

      case 1:
        return Collections.singleton(requireNonNullElement(elements.iterator().next(), 0, parameterName));

      default:
        // create checked copy with load factor = 1.0
        // and capacity of original collection to avoid excessive allocations
        final Set<T> copy = new HashSet<>(elements.size(), 1.0f);
        checkedCopy(elements, copy, parameterName);
        if (copy.size() == 1) {
          return Collections.singleton(copy.iterator().next()); // shortcut to singleton set
        }
        return Collections.unmodifiableSet(copy);
    }
  }

  public static <T> void checkedCopy(Collection<T> from, Collection<T> to, String fromCollectionName) {
    int i = 0;
    for (Iterator<T> it = from.iterator(); it.hasNext(); ++i) {
      to.add(requireNonNullElement(it.next(), i, fromCollectionName));
    }
  }

  private static <T> T requireNonNullElement(@Nullable T element, int position, String collectionName) {
    if (element != null) {
      return element;
    }

    throw new NullPointerException(collectionName + '[' + position + ']');
  }
}
