package jvn.proxy;

import enhanceIrc.EnhancedSentence;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;
import jvn.annotations.Action;

public class JvnProxyHandler implements InvocationHandler {

  private static final String READ = "read";
  private static final String WRITE = "write";
  private static final String EMPTY_STRING = "";
  private Logger logger = Logger.getLogger(JvnProxyHandler.class.getName());
  private JvnObject jo;

  public JvnProxyHandler(Object obj, String name) throws JvnException {
    JvnServerImpl js = JvnServerImpl.jvnGetServer();
    assert js != null;
    this.jo = js.jvnLookupObject(name);
    if (this.jo == null) {
      this.jo = js.jvnCreateObject((Serializable) obj);
      js.jvnRegisterObject(name, this.jo);
      jo.jvnUnLock();
      js.jvnRegisterObject("IRC", jo);
      System.out.println("Object created");
    }
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    System.out.println("Method called");
    String name = method.getName();
    logger.fine("Method name: {}" + name);
    String annotation =
        method.getAnnotation(Action.class) != null ? method.getAnnotation(Action.class).value()
            : EMPTY_STRING;
    System.out.println("Annotation: " + annotation);
    switch (annotation) {
      case READ -> jo.jvnLockRead();
      case WRITE -> jo.jvnLockWrite();
      default -> System.out.println("No annotation found");
    }
    Object result = method.invoke(jo.jvnGetSharedObject(), args);
    System.out.println("result");
    System.out.println(((EnhancedSentence) jo.jvnGetSharedObject()).read() + " dzadzad");
    jo.jvnUnLock();
    return result;
  }
}
