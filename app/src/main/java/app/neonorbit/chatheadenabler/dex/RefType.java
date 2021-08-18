package app.neonorbit.chatheadenabler.dex;

public class RefType {
  private static final int NONE   = 0x0000;
  private static final int ALL    = 0x000F;
  private static final int STRING = 0x0001;
  private static final int FIELD  = 0x0002;
  private static final int METHOD = 0x0004;
  private static final int TYPED  = 0x0008;

  private int flags;

  private RefType() {
    flags = NONE;
  }

  public static RefType get() {
    return new RefType();
  }

  public RefType string() {
    this.flags |= STRING;
    return this;
  }

  public RefType field() {
    this.flags |= FIELD;
    return this;
  }

  public RefType method() {
    this.flags |= METHOD;
    return this;
  }

  public RefType typed() {
    this.flags |= TYPED;
    return this;
  }

  public RefType all() {
    this.flags = ALL;
    return this;
  }

  public boolean any() {
    return (ALL & flags) == ALL;
  }

  public boolean isString() {
    return (STRING & flags) != 0;
  }

  public boolean isField() {
    return (FIELD & flags) != 0;
  }

  public boolean isMethod() {
    return (METHOD & flags) != 0;
  }

  public boolean isTypeDes() {
    return (TYPED & flags) != 0;
  }
}
