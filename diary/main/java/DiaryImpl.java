package main.java;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class DiaryImpl extends UnicastRemoteObject implements DiaryDownloader, DiaryDaemon {

  /** Diary implementation. */
  private HashMap<String, List<Host>> impl = new HashMap<>();
  private HashMap<String, Long> sizes = new HashMap<>();
  Logger logger;
  String address;

  public void setAddress(String address) {
    this.address = address;
  }

  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  public DiaryImpl() throws RemoteException {
    logger = java.util.logging.Logger.getLogger("Diary");
    try {
      address = InetAddress.getLocalHost().getHostAddress();
    } catch (Exception e) {
      System.out.println("Failed to retrieve local address");

    }
  };

  @Override
  public List<Host> request(String file) throws RemoteException, FileIsNotAvailableException {
    System.out.println("REQUEST : \t[" + file + "]");
    List<Host> ret = impl.get(file);
    if (ret == null) {
      throw new FileIsNotAvailableException("No registered host providing this file at the moment.");
    }
    return ret;
  }

  @Override
  public long sizeOf(String file) throws RemoteException, FileIsNotAvailableException {
    System.out.println("SIZEOF : \t[" + file + "]");
    Long ret = sizes.get(file);
    if (ret == null) {
      throw new FileIsNotAvailableException("No registered host providing this file at the moment.");
    }
    return ret.longValue();
  }

  @Override
  public void registerFile(String ip, Integer port, String file, long size) throws RemoteException {
    System.out.println('"' + ip + ':' + port + '"' + "\tregistered: \t" + '[' + file + ']');

    Host h = new Host(ip, port);
    h.addFile(file);
    List<Host> l = impl.get(file);
    if (l == null) {
      l = new LinkedList<>();
      l.add(h);
      impl.put(file, l);
      sizes.put(file, size);
    } else {
      if (sizes.get(file) == size) {
        l.add(h);
      } else {
        throw new RemoteException("File size doesn't match known size");
      }
    }
  }

  @Override
  public List<String> listFiles() {
    List<String> res = new ArrayList<String>();
    res.addAll(impl.keySet());
    return res;
  }
}
