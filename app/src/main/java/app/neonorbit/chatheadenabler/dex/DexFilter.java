package app.neonorbit.chatheadenabler.dex;

import java.util.function.Predicate;

public class DexFilter {
  private final RefType type;
  private final Predicate<String> filter;

  public DexFilter(RefType type, Predicate<String> filter) {
    this.type = type;
    this.filter = filter;
  }

  public boolean verify(String data) {
    return filter.test(data);
  }

  public RefType getType() {
    return type;
  }
}
