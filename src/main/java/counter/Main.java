package counter;

import java.util.logging.Logger;
import jvn.JvnObject;
import jvn.JvnServerImpl;

public class Main {

  private static Logger logger = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) throws InterruptedException {
    try {
      Thread.sleep(500);
      JvnServerImpl js = JvnServerImpl.jvnGetServer();
      JvnObject jo = js.jvnLookupObject("COUNT");
      logger.info("COUNT: JVN server ready...");
      if (jo == null) {
        logger.info("Creating new object");
        jo = js.jvnCreateObject(new Counter());
        jo.jvnUnLock();
        js.jvnRegisterObject("COUNT", jo);
      }
      for (int i = 0; i < 500000; i++) {
        jo.jvnLockWrite();
        ((Counter) jo.jvnGetSharedObject()).increment();
        System.out.println(
            "Value was incremented to " + ((Counter) jo.jvnGetSharedObject()).read());
        jo.jvnUnLock();
        //Thread.sleep(100);
      }
//      while (true) {
//        // generate random boolean value
//        boolean b = Math.random() < 0.5;
//        if (b) {
//          jo.jvnLockWrite();
//          ((Counter) jo.jvnGetSharedObject()).increment();
//          System.out.println(
//              "Value was incremented to " + ((Counter) jo.jvnGetSharedObject()).read());
//          jo.jvnUnLock();
//        } else {
//          jo.jvnLockRead();
//          System.out.println("Read lock acquired");
//          System.out.println("Read value: " + ((Counter) jo.jvnGetSharedObject()).read());
//          jo.jvnUnLock();
//        }
//        Thread.sleep(2000);
//      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
