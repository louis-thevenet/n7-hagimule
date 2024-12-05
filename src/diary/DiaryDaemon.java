

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Diary is the directory that stores file names and 
 * the machines that own them. When a file request is sent, 
 * the requester receives a list of the machines that own it.
 */
public interface DiaryDeamon extends Remote {

    /**
     * Register a fie into the Diary.
     * @param ip The ipAddress of the host
     * @param file The name of the available file
     * @throws RemoteException
     */
    public void registerFile(String ip, String file) throws RemoteException;


}
