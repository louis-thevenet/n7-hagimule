package main.java;

import java.awt.datatransfer.ClipboardOwner;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/** Daemon that registers available files and answers Download requests. */
public class Daemon extends UnicastRemoteObject implements FileProvider {
  HashMap<Integer, String> currentDownloads;
  File[] availableFiles;
  String diaryAddress;
  Integer diaryPort = 8081;

  public void setDiaryAddress(String diaryAddress) {
    this.diaryAddress = diaryAddress;
  }

  public void setDiaryPort(Integer diaryPort) {
    this.diaryPort = diaryPort;
  }

  public void setDaemonAddress(String daemonAddress) {
    this.daemonAddress = daemonAddress;
  }

  public void setDaemonPort(Integer daemonPort) {
    this.daemonPort = daemonPort;
  }

  final String diaryRegisterEndpoint = "/register";

  String daemonAddress;
  Integer daemonPort = 8082;
  final String daemonDownloadEndpoint = "/download";

  /**
   * Creates a new Daemon object
   * 
   * @param available_files_path: Path to the files to make available
   */
  public Daemon(String available_files_path) throws RemoteException {
    File availableFilesDir = new File(available_files_path);
    availableFiles = availableFilesDir.listFiles();
    currentDownloads = new HashMap<>();
    try {
      // Defaults to localhost
      String local = "//" + InetAddress.getLocalHost().getHostAddress();
      diaryAddress = local;
      daemonAddress = local;
    } catch (UnknownHostException e) {
      System.err.println("Could not retrieve local address");
    }
  }

  /**
   * Prints a summary of this Daemon settings
   */
  public void makeSummary() {
    System.out
        .println("Diary address: " + String.join(":", diaryAddress, diaryPort.toString()) + diaryRegisterEndpoint);
    System.out
        .println("Daemon address: " + String.join(":", daemonAddress, daemonPort.toString()) + daemonDownloadEndpoint);

    System.out.println("Files available:");
    for (File f : availableFiles) {
      System.out.println("- " + f.getName());
    }
  }

  /**
   * Registers each files to be made available to the Diary.
   */
  public void notify_diary() {
    System.out.println("Notifying Diary");

    try {
      DiaryDaemon register = (DiaryDaemon) Naming
          .lookup(String.join(":", diaryAddress, diaryPort.toString()) + diaryRegisterEndpoint);

      for (File f : availableFiles) {
        System.out.println("Registering: " + f.getName());
        register.registerFile(daemonAddress, daemonPort, f.getName());
      }
    } catch (Exception ae) {
      System.out.println("Failed to register to diary: " + ae);
    }
  }

  @Override
  public int allocatePortNumber(String client) throws RemoteException {

    int port = daemonPort + 1;
    System.out.println("Allocated port " + port + " for " + client);
    currentDownloads.put(port, client);
    return port;
  }

  @Override
  public void download(String filename, Integer allocatedPort) throws RemoteException {

    String client = currentDownloads.get(allocatedPort);
    try (Socket serverSocket = new Socket(client, allocatedPort)) {
      System.out.println(
          "Sending file over port " + allocatedPort);

      try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()))) {
        File file;
        for (File f : availableFiles) {
          if (f.getName() == filename) {
            file = f;
            FileInputStream fos = new FileInputStream(file);
            int count;
            byte[] buffer = new byte[8192]; // or 4096, or more
            while ((count = fos.read(buffer)) > 0) {
              out.write(buffer, 0, count);
            }
            fos.close();
            return;
          }
        }
      } catch (RemoteException e) {
        throw e;
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } catch (RemoteException e) {
      throw e;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("File not found");
  }

  public void listen() {
    try {
      try {
        LocateRegistry.createRegistry(daemonPort);
      } catch (RemoteException e) {
        LocateRegistry.getRegistry(daemonPort);
      }
      String URL = daemonAddress + ":" + daemonPort + "/download";
      Naming.rebind(URL, (FileProvider) this);
      System.out.println("Listening to requests");
    } catch (Exception e) {
      System.err.println("Server exception: " + e.toString());
      e.printStackTrace();
    }
  }

}
