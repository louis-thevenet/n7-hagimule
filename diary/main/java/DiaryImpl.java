package main.java;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class DiaryImpl extends UnicastRemoteObject implements DiaryDownloader, DiaryDaemon {

  /** Diary implementation. */
  private HashMap<String, List<Host>> impl = new HashMap<>();

  public DiaryImpl() throws RemoteException {
  };

  @Override
  public List<Host> request(String file) throws RemoteException, FileIsNotAvailableException {
    List<Host> ret = impl.get(file);
    if (ret == null) {
      throw new FileIsNotAvailableException("No registered host providing this file at the moment.");
    }
    return ret;
  }

  @Override
  public void registerFile(String ip, String file) throws RemoteException {
    System.out.println('[' + file + ']' + " registered from " + '"' + ip + '"');
    Host h = new Host(ip);
    h.addFile(file);
    List<Host> l = impl.get(file);
    if (l == null) {
      l = new LinkedList<>();
      l.add(h);
      impl.put(file, l);
    } else {
      l.add(h);
    }
  }
}
