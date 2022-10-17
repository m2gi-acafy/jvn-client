package counter;

import java.io.Serializable;
import java.util.logging.Logger;
import jvn.JvnObject;
import jvn.JvnServerImpl;

public class Main {

  private static Logger logger = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) throws InterruptedException {
    try {
      Thread.sleep(1000);
      JvnServerImpl js = JvnServerImpl.jvnGetServer();
      JvnObject jo = js.jvnLookupObject("COUNT");
      logger.info("COUNT: JVN server ready...");
      if (jo == null) {
        logger.info("Creating new object");
        jo = js.jvnCreateObject((Serializable) new Counter());
        jo.jvnUnLock();
        js.jvnRegisterObject("COUNT", jo);
      }
      for( int i = 0; i < 50000; i++) {
        // generate random boolean value
       // boolean b = Math.random() < 0.5;
        Boolean b = true;
        if (b) {
          Thread.sleep(10);
          jo.jvnLockWrite();
          ((Counter) jo.jvnGetSharedObject()).increment();
          System.out.println(
              "Value was incremented to " + ((Counter) jo.jvnGetSharedObject()).read());
          jo.jvnUnLock();
        } else {
          jo.jvnLockRead();
          System.out.println("Read lock acquired");
          System.out.println("Read value: " + ((Counter) jo.jvnGetSharedObject()).read());
          jo.jvnUnLock();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
