package main.java;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DiaryDownloader extends Remote {
  /**
   * Get the names of the Hosts who get the file.
   *
   * @param file the filename wanted
   * @return the list of Hosts
   * @throws RemoteException
   * @throws FileIsNotAvailableException If there is not a Hosts who get the File
   */
  public List<Host> whichHosts(String file) throws RemoteException, FileIsNotAvailableException;
}
