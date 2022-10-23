/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
 *
 * Authors: 
 */

package jvn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {


  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private static JvnCoordImpl coord;
  private transient Registry registry;

  private Map<Integer, List<JvnRemoteServer>> readers;

  private Map<Integer, JvnRemoteServer> writers;

  private Map<Integer, JvnObject> objects;

  private Map<Integer, String> joiAndJonsMap;


  /**
   * Default constructor
   *
   * @throws JvnException
   **/
  private JvnCoordImpl() throws Exception {
    super();
    objects = new ConcurrentHashMap<>();
    readers = new ConcurrentHashMap<>();
    writers = new ConcurrentHashMap<>();
    joiAndJonsMap = new HashMap<>();
    registry = registry == null ? LocateRegistry.createRegistry(1099) : registry;
    registry.bind("JvnCoord", this);
    loadState();
  }

  public static JvnCoordImpl getInstance() throws Exception {
    return coord == null ? new JvnCoordImpl() : coord;
  }

  /**
   * Allocate a NEW JVN object id (usually allocated to a newly created JVN object)
   *
   * @throws RemoteException,JvnException
   **/
  public int jvnGetObjectId() throws RemoteException, JvnException {
    return UUID.randomUUID().hashCode() & Integer.MAX_VALUE;
  }

  /**
   * Associate a symbolic name with a JVN object
   *
   * @param jon : the JVN object name
   * @param jo  : the JVN object
   * @param joi : the JVN object identification
   * @param js  : the remote reference of the JVNServer
   * @throws RemoteException,JvnException
   **/
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
      throws RemoteException, JvnException {
    objects.put(jo.jvnGetObjectId(), jo);
    writers.put(jo.jvnGetObjectId(), js);
    joiAndJonsMap.put(jo.jvnGetObjectId(), jon);
    try {
      saveState();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Get the reference of a JVN object managed by a given JVN server
   *
   * @param jon : the JVN object name
   * @param js  : the remote reference of the JVNServer
   * @throws RemoteException,JvnException
   **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
      throws java.rmi.RemoteException, JvnException {
    JvnObject result = objects.values().stream().filter(jvnObject -> {
      try {
        return joiAndJonsMap.get(jvnObject.jvnGetObjectId()).equals(jon);
      } catch (JvnException e) {
        throw new RuntimeException(e);
      }
    }).findFirst().orElse(null);
    // Quand tu récupères un object, tu n'as pas le lock dessus
    if (result != null) {
      result.jvnResetLock();
    }
    return result;

  }

  /**
   * Get a Read lock on a JVN object managed by a given JVN server
   *
   * @param joi : the JVN object identification
   * @param js  : the remote reference of the server
   * @return the current JVN object state
   * @throws RemoteException, JvnException Cohérence : cas de figure 6
   **/
  public Serializable jvnLockRead(int joi, JvnRemoteServer js)
      throws RemoteException, JvnException {
    synchronized (this) {
      if (writers.containsKey(joi)) {
        JvnRemoteServer writer = writers.get(joi);
        JvnObject object = (JvnObject) writer.jvnInvalidateWriterForReader(joi);
        objects.put(joi, object);
        readers.computeIfAbsent(joi, k -> Collections.synchronizedList(new ArrayList<>()))
            .add(writer);
        writers.remove(joi);
      }
      readers.computeIfAbsent(joi, k -> Collections.synchronizedList(new ArrayList<>())).add(js);
      try {
        saveState();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return objects.get(joi).jvnGetSharedObject();
    }
  }

  /**
   * Get a Write lock on a JVN object managed by a given JVN server
   *
   * @param joi : the JVN object identification
   * @param js  : the remote reference of the server
   * @return the current JVN object state
   * @throws RemoteException, JvnException
   **/
  public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js)
      throws RemoteException, JvnException {
    List<JvnRemoteServer> temp = new ArrayList<>();
    // On invalide les readers et on les stocke dans une liste temporaire
    readers.forEach((key, value) -> {
      if (key.equals(joi)) {
        value.forEach(jvnRemoteServer -> {
          try {
            if (!jvnRemoteServer.equals(js)) {
              jvnRemoteServer.jvnInvalidateReader(joi);
            }
            temp.add(jvnRemoteServer);
          } catch (RemoteException | JvnException e) {
            throw new RuntimeException(e);
          }
        });
      }
    });
    // On supprime les readers de la liste principale
    if (readers.containsKey(joi)) {
      readers.get(joi).removeAll(temp);
    }
    JvnRemoteServer writer = writers.get(joi);
    // On invalide le writer
    if (writer != null && !writer.equals(js)) {
      JvnObject jvnObject = (JvnObject) writer.jvnInvalidateWriter(joi);
      objects.put(joi, jvnObject);
      writers.remove(joi);
    }
    // On ajoute le writer
    writers.put(joi, js);
    try {
      saveState();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return objects.get(joi).jvnGetSharedObject();
  }

  /**
   * A JVN server terminates
   *
   * @param js : the remote reference of the server
   * @throws RemoteException, JvnException
   **/
  public synchronized void jvnTerminate(JvnRemoteServer js) throws RemoteException, JvnException {
    readers.forEach((key, value) -> {
      value.forEach(jvnRemoteServer -> {
        if (jvnRemoteServer.equals(js)) {
          readers.get(key).remove(jvnRemoteServer);
        }
      });
    });
    writers.forEach((key, value) -> {
      if (value.equals(js)) {
        writers.remove(key);
      }
    });
  }

  private void loadState() throws IOException, ClassNotFoundException {
    File file = new File("coord.ser");
    if (file.exists()) {
      FileInputStream fileIn = new FileInputStream(file);
      ObjectInputStream in = new ObjectInputStream(fileIn);
      JvnCoordImpl coord = (JvnCoordImpl) in.readObject();
      this.readers = coord.readers;
      this.writers = coord.writers;
      this.objects = coord.objects;
      System.out.println(objects.size());
      this.joiAndJonsMap = coord.joiAndJonsMap;
      in.close();
      fileIn.close();
    }
  }

  private void saveState() throws IOException, IOException {
    FileOutputStream fileOut = new FileOutputStream("coord.ser");
    ObjectOutputStream out = new ObjectOutputStream(fileOut);
    out.writeObject(this);
    out.close();
    fileOut.close();
  }

}

 
