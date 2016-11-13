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
