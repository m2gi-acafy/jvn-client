package counter;

public class Counter {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  int data;

  public Counter() {
    data = 0;
  }

  public void increment() {
    data++;
  }

  public int read() {
    return data;
  }

}
