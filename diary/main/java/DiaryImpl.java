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
  private List<Host> allTheHost = new ArrayList<>();
  Logger logger;
  String address;
  private long lastVerif = System.currentTimeMillis();

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

  private Host findHost(String ip, Integer port) {
    Host ret = new Host(ip, port);
    for (Host host : allTheHost) {
      if (host.equals(ret)) {
        return host;
      }
    }
    return null;
  }

  @Override
  public void registerFile(String ip, Integer port, String file, long size) throws RemoteException {
    System.out.println('"' + ip + ':' + port + '"' + "\tregistered: \t" + '[' + file + ']');

    // get the host from the list of all Hosts
    Host h = findHost(ip, port);
    if (h == null) {
      h = new Host(ip, port);
      allTheHost.add(h);
    }
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

  @Override
  public void disconnect(String ip, Integer port) throws RemoteException {
    System.out.println("DISCONNECT : \t [" + ip + ":" + port + "]");
    
    // get the Host
    Host h = findHost(ip, port);
    if (h != null) {
      for (String file : h.getFiles()) {
        List<Host> assoc = impl.get(file);
        assoc.remove(h);
        if (assoc.isEmpty()) {
          impl.remove(file);
        }
      }
      allTheHost.remove(h);
    }
  }

  @Override
  public boolean notifyAlive(String ip, Integer port) throws RemoteException {
    System.out.println("NOTIFY ALIVE: \t [" + ip + ":" + port + "]");
    boolean found = false;

    Host h = findHost(ip, port);
    if (h != null) {
      found = true;
      h.resetTime();
    }

    // lance une vérification globale si l'interval de confiance expire
    if (System.currentTimeMillis() - lastVerif > 90_000) {
      verifAlive();
    }
    return found;
  }

  private void verifAlive() {
    for (Host host : allTheHost) {
      if (System.currentTimeMillis() - host.getTime() > 85_000) {
        try {
          disconnect(host.getIp(), host.getPort());
        } catch (Exception e) {
        }
      }
    }
  }
}
