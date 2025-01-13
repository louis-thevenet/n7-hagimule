package main.java;

import java.rmi.Remote;
import java.rmi.RemoteException;


/** FileProvider makes the links between a Downloader and a Daemon. */
public interface FileProvider extends Remote {

  /**
   * define a port to the daemon and launch a sender Thread.
   * @param downloaderAddress adress of the downloader.
   * @param downloaderPort port of the downloader.
   * @param filename name of the file.
   * @param offset offset of the download part.
   * @param size size of the block to send.
   * @return the port of the deamon open for the sender thread.
   * @throws RemoteException if the file is not available.
   */
  int download(String downloaderAddress, int downloaderPort, String filename, long offset, long size) throws RemoteException;
}
