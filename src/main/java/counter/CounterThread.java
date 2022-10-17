package counter;

import java.io.Serializable;
import java.util.logging.Logger;
import jvn.JvnObject;
import jvn.JvnServerImpl;

public class CounterThread implements Runnable {
  private static Logger logger = Logger.getLogger(CounterThread.class.getName());

  private String name;

  public CounterThread(String s) {
    name = s;
  }

  @Override
  public void run() {
    try {
      JvnServerImpl js = JvnServerImpl.jvnGetServer();
      JvnObject jo = js.jvnLookupObject("COUNT");
      logger.info("COUNT: JVN server ready...");
      if (jo == null) {
        logger.info("Creating new object");
        jo = js.jvnCreateObject((Serializable) new Counter());
        jo.jvnUnLock();
        System.out.println(name+" Is registering the object");
        js.jvnRegisterObject("COUNT", jo);
      }
      while (true) {
        System.out.println(name);
        // generate random boolean value
        boolean b = Math.random() < 0.5;
        if (b) {
          System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
          System.out.println("Counter Thread" + name + " is writing");
          jo.jvnLockWrite();
          ((Counter) jo.jvnGetSharedObject()).increment();
          System.out.println(
              "Value was incremented to " + ((Counter) jo.jvnGetSharedObject()).read());
          jo.jvnUnLock();
          System.out.println("Counter Thread" + name + " is done writing");
          System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        } else {
          System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
          System.out.println("Counter Thread" + name + " is reading");
          jo.jvnLockRead();
          System.out.println("Read lock acquired");
          System.out.println("Read value: " + ((Counter) jo.jvnGetSharedObject()).read());
          jo.jvnUnLock();
          System.out.println("Counter Thread" + name + " is done reading");
          System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        }
        Thread.sleep(4500);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
