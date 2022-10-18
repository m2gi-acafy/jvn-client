package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import static java.lang.Thread.currentThread;

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
    public void jvnLockWrite() throws JvnException {
        switch (lockState) {
            case WC, RWC -> lockState = JvnLock.W;
            case NL, RC, R -> {
                System.out.println("calling the coord");
                object = JvnServerImpl.jvnGetServer().jvnLockWrite(id);
                System.out.println("okay i can continue");
                lockState = JvnLock.W;
            }
            default -> throw new JvnException("Write lock not possible");
        }
        System.out.println(lockState);
    }

    @Override
    public void jvnUnLock() throws JvnException {
        System.out.println("["+ currentThread(). getName()+"] I'm unlocking object");
        System.out.println(lockState);
        synchronized (this) {
            switch (lockState) {
                case R -> lockState = JvnLock.RC;
                case W, RWC -> lockState = JvnLock.WC;
                default -> throw new JvnException("Unlock not possible");
            }
            notifyAll();
        }
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
        synchronized (this) {
            switch (lockState) {
                case R, RWC -> {
                    while (lockState == JvnLock.R) {
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
                    System.out.println("invalidate reader put NL lock");
                }
                case NL -> {
                    break;
                }
                default -> throw new JvnException("Invalid lock state");
            }
        }
    }

    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
            switch (lockState) {
                case W, RWC -> {
                    while (lockState == JvnLock.W || lockState == JvnLock.RWC) {
                        try {
                            System.out.println("["+ currentThread(). getName()+"] I'm gonna wait");
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    lockState = JvnLock.NL;
                    System.out.println("["+ currentThread(). getName()+"] invalidate writer put NL lock from W after wait");
                }
                case WC -> {
                    lockState = JvnLock.NL;
                    System.out.println("invalidate writer put NL lock from WC");
                }
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
