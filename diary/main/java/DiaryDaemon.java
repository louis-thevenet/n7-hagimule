package main.java;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Diary is the directory that stores file names and the machines that own them.
 * When a file request
 * is sent, the requester receives a list of the machines that own it.
 */
public interface DiaryDaemon extends Remote {

  /**
   * Register a fie into the Diary.
   *
   * @param ip   The ipAddress of the host
   * @param file The name of the available file
   * @throws RemoteException if the size is different
   */
  public void registerFile(String ip, Integer port, String file, long size) throws RemoteException;

  /**
   * Disconnect a host of files.
   * @param ip ip of the host
   * @param port port of the host
   * @throws RemoteException
   */
  public void disconnect(String ip, Integer port) throws RemoteException;
}
