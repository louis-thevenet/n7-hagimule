package main.java;

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
  Logger logger;

  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  public DiaryImpl() throws RemoteException {
    logger = java.util.logging.Logger.getLogger("Diary");
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
    logger.fine('"' + ip + '"' + "\tregistered: \t" + '[' + file + ']');

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

  @Override
  public List<String> listFiles() {
    List<String> res = new ArrayList<String>();
    res.addAll(impl.keySet());
    return res;
  }
}
