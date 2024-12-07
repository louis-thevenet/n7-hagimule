package main.java;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DiaryDownloader extends Remote {
  /**
   * Returns a list of hosts that provide the requested file.
   *
   * @param file name of the requested file
   * @return the list of Hosts
   * @throws RemoteException
   * @throws FileIsNotAvailableException If there is not a Hosts who get the File
   */
  public List<Host> request(String file) throws RemoteException, FileIsNotAvailableException;
}
