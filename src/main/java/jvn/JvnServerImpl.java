/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


public class JvnServerImpl
    extends UnicastRemoteObject
    implements JvnLocalServer, JvnRemoteServer {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private Logger logger = Logger.getLogger(JvnServerImpl.class.getName());
  // A JVN server is managed as a singleton
  private static JvnServerImpl js = null;
  private transient JvnRemoteCoord coord;
  private transient Registry registry;
  private Map<Integer, JvnObject> objects;


  /**
   * Default constructor
   *
   * @throws JvnException
   **/
  private JvnServerImpl() throws Exception {
    super();
    objects = new ConcurrentHashMap<>();
    registry = LocateRegistry.getRegistry(1099);
    coord = (JvnRemoteCoord) registry.lookup("JvnCoord");

  }

  /**
   * Static method allowing an application to get a reference to a JVN server instance
   *
   * @throws JvnException
   **/
  public static JvnServerImpl jvnGetServer() {
    if (js == null) {
      try {
        js = new JvnServerImpl();
      } catch (Exception e) {
        return null;
      }
    }
    return js;
  }

  /**
   * The JVN service is not used anymore
   *
   * @throws JvnException
   **/
  public void jvnTerminate()
      throws JvnException {
    try {
      coord.jvnTerminate(this);
    } catch (RemoteException e) {
      throw new JvnException(e.getMessage());
    }

  }

  /**
   * creation of a JVN object
   *
   * @param o : the JVN object state
   * @throws JvnException
   **/
  public synchronized JvnObject jvnCreateObject(Serializable o)
      throws JvnException {
    try {
      var id = coord.jvnGetObjectId();
      var jvnObject = new JvnObjectImpl(id, o);
      objects.put(id, jvnObject);
      return jvnObject;
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }

  }


  /**
   * Associate a symbolic name with a JVN object
   *
   * @param jon : the JVN object name
   * @param jo  : the JVN object
   * @throws JvnException
   **/
  public void jvnRegisterObject(String jon, JvnObject jo)
      throws JvnException {
    try {
      objects.put(jo.jvnGetObjectId(), jo);
      coord.jvnRegisterObject(jon, jo, this);

    } catch (RemoteException e) {
      throw new JvnException(e.getMessage());
    }
  }

  /**
   * Provide the reference of a JVN object beeing given its symbolic name
   *
   * @param jon : the JVN object name
   * @return the JVN object
   * @throws JvnException
   **/
  public JvnObject jvnLookupObject(String jon)
      throws JvnException {

    try {
      var jvnObject = coord.jvnLookupObject(jon, this);
      if (jvnObject != null) {
        objects.put(jvnObject.jvnGetObjectId(), jvnObject);
      }
      return jvnObject;
    } catch (RemoteException e) {
      throw new JvnException(e.getMessage());
    }
  }

  /**
   * Get a Read lock on a JVN object
   *
   * @param joi : the JVN object identification
   * @return the current JVN object state
   * @throws JvnException
   **/
  public Serializable jvnLockRead(int joi)
      throws JvnException {
    try {
      return coord.jvnLockRead(joi, this);
    } catch (RemoteException e) {
      throw new JvnException(e.getMessage());
    }

  }

  /**
   * Get a Write lock on a JVN object
   *
   * @param joi : the JVN object identification
   * @return the current JVN object state
   * @throws JvnException
   **/
  public Serializable jvnLockWrite(int joi)
      throws JvnException {
    try {
      return coord.jvnLockWrite(joi, this);
    } catch (RemoteException e) {
      throw new JvnException(e.getMessage());
    }
  }


  /**
   * Invalidate the Read lock of the JVN object identified by id called by the JvnCoord
   *
   * @param joi : the JVN object id
   * @return void
   * @throws RemoteException,JvnException
   **/
  public void jvnInvalidateReader(int joi)
      throws RemoteException, JvnException {
    objects.get(joi).jvnInvalidateReader();
  }

  /**
   * Invalidate the Write lock of the JVN object identified by id
   *
   * @param joi : the JVN object id
   * @return the current JVN object state
   * @throws RemoteException,JvnException
   **/
  public Serializable jvnInvalidateWriter(int joi)
      throws RemoteException, JvnException {
    return objects.get(joi).jvnInvalidateWriter();
  }

  ;

  /**
   * Reduce the Write lock of the JVN object identified by id
   *
   * @param joi : the JVN object id
   * @return the current JVN object state
   * @throws RemoteException,JvnException
   **/
  public Serializable jvnInvalidateWriterForReader(int joi)
      throws RemoteException, JvnException {
    System.out.println("jvnInvalidateWriterForReader server");
    Serializable serializable = objects.get(joi).jvnInvalidateWriterForReader();
    return serializable;
  }

  ;
}

 
