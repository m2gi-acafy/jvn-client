package jvn;

import static jvn.JvnLock.WC;

import java.io.Serializable;
import java.rmi.RemoteException;

public class JvnObjectImpl implements JvnObject {

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
  public synchronized void jvnLockRead() throws JvnException {
    switch (lockState) {
      case RC -> lockState = JvnLock.R;
      case WC -> lockState = JvnLock.RWC;
      case NL -> {
        object = JvnServerImpl.jvnGetServer().jvnLockRead(id);
        lockState = JvnLock.R;
      }
      default -> throw new JvnException("Read lock not possible");
    }
  }

  @Override
  public synchronized void jvnLockWrite() throws JvnException {
    switch (lockState) {
      case WC, RWC -> lockState = JvnLock.W;
      case NL, RC, R -> {
        object = JvnServerImpl.jvnGetServer().jvnLockWrite(id);
        lockState = JvnLock.W;
      }
      default -> throw new JvnException("Write lock not possible");
    }
  }


  @Override
  public synchronized void jvnUnLock() throws JvnException {
    switch (lockState) {
      case R -> lockState = JvnLock.RC;
      case W, RWC -> lockState = WC;
      default -> throw new JvnException("Unlock not possible");
    }
    notify();
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
  public synchronized void jvnInvalidateReader() throws JvnException, RemoteException {

    switch (lockState) {
      case R, RWC -> {
        while (lockState == JvnLock.R || lockState == JvnLock.RWC) {
          try {
            wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        lockState = lockState.NL;
      }
      case RC -> {
        lockState = JvnLock.NL;
      }
      case NL -> {
        break;
      }
      default -> throw new JvnException("Invalid lock state");
    }

  }

  @Override
  public synchronized Serializable jvnInvalidateWriter() throws JvnException {
    switch (lockState) {
      case W, RWC -> {
        while (lockState == JvnLock.W || lockState == JvnLock.RWC) {
          try {
            wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        lockState = JvnLock.NL;
      }
      case WC -> lockState = JvnLock.NL;
      case NL -> {
        break;
      }
      default -> throw new JvnException("Invalid lock state");
    }

    return this;
  }

  @Override
  public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
    switch (lockState) {
      case RWC -> lockState = JvnLock.R;
      case WC -> lockState = JvnLock.RC;
      case NL -> {
        break;
      }
      case W -> {
        while (lockState == JvnLock.W) {
          try {
            wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        lockState = JvnLock.RC;
      }
      default -> throw new JvnException("Invalid Lock state");
    }
    return this;
  }

  @Override
  public void jvnResetLock() {
    lockState = JvnLock.NL;
  }

}
