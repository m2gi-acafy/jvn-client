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
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class JvnServerImpl
    extends UnicastRemoteObject
    implements JvnLocalServer, JvnRemoteServer {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private static final Integer MAX_CACHE_SIZE = 3;
  private static JvnServerImpl js = null;
  private transient JvnRemoteCoord coord;
  private transient Registry registry;
  private Map<Integer, JvnObject> localObjects;
  private Queue<Integer> cachedObjectsIds;


  /**
   * Default constructor
   *
   * @throws JvnException
   **/
  private JvnServerImpl() throws Exception {
    super();
    localObjects = new ConcurrentHashMap<>();
    lookupCoord();
    cachedObjectsIds = new ConcurrentLinkedQueue<>();
  }

  private void lookupCoord() throws Exception {
    registry = registry == null ? LocateRegistry.getRegistry(1099) : registry;
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
      lookupCoord();
      coord.jvnTerminate(this);
      System.out.println("local objects : " + localObjects.size());
      localObjects.clear();
      cachedObjectsIds.clear();
    } catch (RemoteException e) {
      throw new JvnException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
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
      lookupCoord();
      if (localObjects.size() >= MAX_CACHE_SIZE) {
        Integer id = cachedObjectsIds.poll();
        localObjects.remove(id);
      }
      var id = coord.jvnGetObjectId();
      var jvnObject = new JvnObjectImpl(id, o);
      localObjects.put(id, jvnObject);
      cachedObjectsIds.add(id);
      return jvnObject;
    } catch (Exception e) {
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
      lookupCoord();
      coord.jvnRegisterObject(jon, jo, this);
    } catch (RemoteException e) {
      throw new JvnException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
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
      lookupCoord();
      var jvnObject = coord.jvnLookupObject(jon, this);
      if (jvnObject != null) {
        if (localObjects.size() >= MAX_CACHE_SIZE) {
          Integer id = cachedObjectsIds.poll();
          localObjects.remove(id);
        }
        localObjects.put(jvnObject.jvnGetObjectId(), jvnObject);
        cachedObjectsIds.add(jvnObject.jvnGetObjectId());
      }
      return jvnObject;
    } catch (Exception e) {
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
      lookupCoord();
      return coord.jvnLockRead(joi, this);
    } catch (Exception e) {
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
      lookupCoord();
      return coord.jvnLockWrite(joi, this);
    } catch (Exception e) {
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
    localObjects.get(joi).jvnInvalidateReader();
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
    return localObjects.get(joi).jvnInvalidateWriter();
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
    return localObjects.get(joi).jvnInvalidateWriterForReader();
  }

  ;
}

 
