package hagimule;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class App {
  static final int default_port = 1000;

  static String get_files_path(String[] args) {
    String available_files_path;
    if (args.length >= 1) {
      available_files_path = args[0];
    } else {
      String home = System.getProperty("user.home");
      available_files_path = home + "/Downloads/";
    }
    return available_files_path;

  }

  static int get_port(String[] args) {
    int port = default_port;
    if (args.length >= 2) {
      try {
        port = Integer.parseInt(args[1]);
      } catch (Exception e) {

      }
    }
    return port;

  }

  public static void main(String[] args) {
    Daemon daemon = new Daemon(get_files_path(args));
    int port = get_port(args);

    daemon.notify_diary();
    try {
      FileProvider stub = (FileProvider) UnicastRemoteObject.exportObject(daemon, 0);
      Registry registry;
      try {
        registry = LocateRegistry.createRegistry(port);
      } catch (RemoteException e) {
        registry = LocateRegistry.getRegistry(port);
      }
      registry.rebind("FileProvider", stub);

      System.err.println("Server ready");
    } catch (Exception e) {
      System.err.println("Server exception: " + e.toString());
      e.printStackTrace();
    }
  }

}
