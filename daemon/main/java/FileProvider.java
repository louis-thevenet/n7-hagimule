package main.java;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileProvider extends Remote {
  int allocatePortNumber(String client) throws RemoteException;

  void download(String filename, Integer allocatedPort) throws RemoteException;
}
