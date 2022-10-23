package localCache;


import java.util.logging.Logger;
import jvn.JvnCoordImpl;

public class Coordinateur {

  static Logger logger = Logger.getLogger(Coordinateur.class.getName());

  public static void main(String[] args) {
    try {
      JvnCoordImpl.getInstance();
      logger.info("Javanaise started");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
