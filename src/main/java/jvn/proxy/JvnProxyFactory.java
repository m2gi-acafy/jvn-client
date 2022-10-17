package jvn.proxy;

import static java.lang.reflect.Proxy.newProxyInstance;

import java.lang.reflect.InvocationHandler;

public class JvnProxyFactory {

  private JvnProxyFactory() {

  }

  public static Object newInstance(Object object, String name)
      throws Exception {
    InvocationHandler handler = new JvnProxyHandler(object, name);
    return newProxyInstance(
        object.getClass().getClassLoader(),
        object.getClass().getInterfaces(),
        handler);
  }

}
