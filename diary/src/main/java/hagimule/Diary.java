package hagimule;

import java.util.HashMap;
import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Diary is the directory that stores file names and 
 * the machines that own them. When a file request is sent, 
 * the requester receives a list of the machines that own it.
 */
public interface Diary extends Remote {

    /**
     * Get the names of the Hosts who get the file.
     * @param file the filename wanted
     * @return the list of Hosts
     * @throws RemoteException
     * @throws FileIsNotAvailableException If there is not a Hosts who get the File
     */
    public List<Host> whichHosts(String file)
        throws RemoteException, FileIsNotAvailableException;

}
