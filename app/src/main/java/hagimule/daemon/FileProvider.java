package hagimule.daemon;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileProvider extends Remote {
  void Download(String filename) throws RemoteException;
}
