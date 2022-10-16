package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Logger;

public class JvnObjectImpl implements JvnObject {

  private static Logger logger = Logger.getLogger(JvnObjectImpl.class.getName());
  /**
   *
   */
  private static final long serialVersionUID = 1L;


  Serializable object;
  int id;
  JvnLock lockState;


  public JvnObjectImpl(int id, Serializable object) {
    this.object = object;
    // after creation , the object is in write mode
    this.id = id;
    this.lockState = JvnLock.W;

  }

  @Override
  public void jvnLockRead() throws JvnException {
    System.out.println("jvnLockRead");
    System.out.println(lockState);
    switch (lockState) {
      case RC -> lockState = JvnLock.R;
      case WC -> lockState = JvnLock.RWC;
      case NL -> {
        System.out.println("calling the coord to lock read");
        object = JvnServerImpl.jvnGetServer().jvnLockRead(id);
        System.out.println("object after lock read");
        //System.out.println("read object" + ((Counter) object).read());
        lockState = JvnLock.R;
      }
      default -> throw new JvnException("Read lock not possible");
    }
    System.out.println(lockState);
  }

  @Override
  public void jvnLockWrite() throws JvnException {
    System.out.println("JvnObject " + " jvnLockWrite method");
    System.out.println("lock state before write lock" + lockState);
    switch (lockState) {
      case WC, RWC -> lockState = JvnLock.W;
      case NL, RC, R -> {
        System.out.println("calling the coord to lock write");
        object = JvnServerImpl.jvnGetServer().jvnLockWrite(id);
        lockState = JvnLock.W;
      }
      default -> throw new JvnException("Write lock not possible");
    }
    System.out.println("lock state after write lock" + lockState);
  }

  @Override
  public synchronized void jvnUnLock() throws JvnException {
    System.out.println("JvnObject " + " jvnUnLock method");
    System.out.println("lock state before unlock" + lockState);
    switch (lockState) {
      case R -> lockState = JvnLock.RC;
      case W, RWC -> lockState = JvnLock.WC;
      default -> throw new JvnException("Unlock not possible");
    }
    this.notifyAll();
    System.out.println("lock state after unlock" + lockState);
  }

  @Override
  public int jvnGetObjectId() throws JvnException {
    return id;
  }

  @Override
  public Serializable jvnGetSharedObject() throws JvnException {
    return object;
  }

  @Override
  public void jvnInvalidateReader() throws JvnException, RemoteException {
    System.out.println("JvnObject " + " jvnInvalidateReader method");
    System.out.println("lock state before invalidate reader" + lockState);
    synchronized (lockState) {
      switch (lockState) {
        case R, RWC -> {
          synchronized (lockState) {
            while (lockState == JvnLock.R) {
              try {
                lockState.wait();
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
            lockState = lockState.NL;
          }
        }
        case RC -> lockState = JvnLock.NL;
        case NL -> {
          break;
        }
        default -> throw new JvnException("Invalid lock state");
      }
    }

    System.out.println("lock state after invalidate reader" + lockState);
  }

  @Override
  public synchronized Serializable jvnInvalidateWriter() throws JvnException {
    System.out.println("jvn Object : invaldiate writer ");
    System.out.println("lock state before invalidate writer" + lockState);
    switch (lockState) {
      case W, RWC -> {
        while (lockState == JvnLock.W || lockState == JvnLock.RWC) {
          try {
            lockState.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          lockState = JvnLock.NL;
        }
      }
      case WC -> lockState = JvnLock.NL;
      case NL -> {
        break;
      }
      default -> throw new JvnException("Invalid lock state");
    }
    System.out.println("lock state after invalidate writer" + lockState);
    return this;
  }

  @Override
  public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
    System.out.println("jvnObject " + " jvnInvalidateWriterForReader method");
    System.out.println("lock state before invalidate writer for reader" + lockState);
    switch (lockState) {
      case RWC -> lockState = JvnLock.R;
      case WC -> lockState = JvnLock.RC;
      case NL -> {
        break;
      }
      case W -> {
        while (lockState == JvnLock.W) {
          try {
            lockState.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        lockState = JvnLock.RC;
      }
      default -> throw new JvnException("Invalid Lock state");
    }
    System.out.println("lock state after invalidate writer for reader" + lockState);
    return this;
  }

  @Override
  public void jvnResetLock() {
    lockState = JvnLock.NL;
  }

}
