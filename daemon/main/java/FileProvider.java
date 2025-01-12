package main.java;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileProvider extends Remote {
  int download(String client, int downloaderPort, String filename, long offset, long size) throws RemoteException;

}
