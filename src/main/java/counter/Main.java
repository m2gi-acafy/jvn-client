package counter;

import java.util.logging.Logger;

public class Main {

  private static Logger logger = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) throws InterruptedException {
    logger.info("Starting main");
    Thread t1 = new Thread(new CounterThread("thread 1"));
    Thread t2 = new Thread(new CounterThread("thread 2"));
    t1.start();
    Thread.sleep(1000);
    t2.start();
    t1.join();
    t2.join();
  }

}
