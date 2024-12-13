package main.java;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileProvider extends Remote {
  int download(String client, String filename) throws RemoteException;

}
