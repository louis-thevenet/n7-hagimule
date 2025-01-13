package main.java;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/** DiaryImpl is a server which get the name of Hosts and the files they can provide. */
public class DiaryImpl extends UnicastRemoteObject implements DiaryDownloader, DiaryDaemon {

  /** Diary implementation : a hashMap of filenames and a list of hosts. */
  private HashMap<String, List<Host>> impl = new HashMap<>();
  /** a HashMap of filenames and int for the size. */
  private HashMap<String, Long> sizes = new HashMap<>();
  /** List of all the Host register in the Diary. */
  private List<Host> allTheHost = new ArrayList<>();

  /** Logger register the activity. */
  Logger logger;
  
  /** The ip address of the diary. */
  String address;
  
  /** The time of the last verification of alive daemons. */
  private long lastVerif = System.currentTimeMillis();

  /**
   * Set the address of the diary.
   * @param address the new value.
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * Set the logger.
   * @param logger the new value.
   */
  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  /**
   * Builder a Diary
   * @throws RemoteException
   */
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
    // check daemon alive
    verifAliveIfNecessary();
    System.out.println("REQUEST : \t[" + file + "]");

    // get the list of Host which provide
    List<Host> ret = impl.get(file);
    if (ret == null) {
      throw new FileIsNotAvailableException("No registered host providing this file at the moment.");
    }
    return ret;
  }

  @Override
  public long sizeOf(String file) throws RemoteException, FileIsNotAvailableException {
    // check daemon alive
    verifAliveIfNecessary();
    System.out.println("SIZEOF : \t[" + file + "]");
    Long ret = sizes.get(file);
    if (ret == null) {
      throw new FileIsNotAvailableException("No registered host providing this file at the moment.");
    }
    return ret.longValue();
  }

  /**
   * Finds the host in the allTheHost list.
   * @param ip the ip of the Host.
   * @param port the port of the Host.
   * @return the Host or null if not found.
   */
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
    verifAliveIfNecessary();
    System.out.println('"' + ip + ':' + port + '"' + "\tregistered: \t" + '[' + file + ']');

    // get the host from the list of all Hosts
    Host h = findHost(ip, port);
    if (h == null) {
      h = new Host(ip, port);
      allTheHost.add(h);
    }
    // add the file to the host in the list
    h.addFile(file);

    // get the list for the file
    List<Host> l = impl.get(file);

    // create a list
    if (l == null) {
      l = new LinkedList<>();
      l.add(h);
      impl.put(file, l);
      sizes.put(file, size);
    } else {
      // check if the size of the file is the same
      if (sizes.get(file) == size) {
        l.add(h);
      } else {
        throw new RemoteException("File size doesn't match known size");
      }
    }
  }

  @Override
  public List<String> listFiles() {
    System.out.println("LIST_OF_FILES");
    verifAliveIfNecessary();
    List<String> res = new ArrayList<String>();
    res.addAll(impl.keySet());
    return res;
  }

  /**
   * Remove a host from all list, remove the file if there 
   * are no more provider.
   * @param h the host to remove.
   */
  private void removeFromLists(Host h) {
    Iterator<String> iter = h.getFiles().iterator();
    while (iter.hasNext()) {
      String file = iter.next();
      List<Host> assoc = impl.get(file);
      assoc.remove(h);
      if (assoc.isEmpty()) {
        impl.remove(file);
      }
    }
    allTheHost.remove(h);
  }

  @Override
  public void disconnect(String ip, Integer port) throws RemoteException {
    System.out.println("DISCONNECT : \t [" + ip + ":" + port + "]");

    // get the Host
    Host h = findHost(ip, port);
    removeFromLists(h);
  }

  /**
   * launch verifAlive if the last check is too much late.
   */
  private void verifAliveIfNecessary() {
    // lance une vérification globale si l'interval de confiance expire
    if (System.currentTimeMillis() - lastVerif > 90_000) {
      verifAlive();
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
    return found;
  }

  /**
   * Check if each host is still alive.
   */
  private void verifAlive() {
    for (Host host : allTheHost) {
      // no signal 85 s -> remove from lists
      if (System.currentTimeMillis() - host.getTime() > 85_000) {
        try {
          removeFromLists(host);
        } catch (Exception e) {
        }
      }
    }
  }
}
