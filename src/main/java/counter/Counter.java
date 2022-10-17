package counter;

public class Counter {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  String data;

  public Counter() {
    data = String.valueOf(0);
  }

  public void increment() {
    data = String.valueOf(Integer.parseInt(data) + 1);
  }

  public String read() {
    return data;
  }

}
