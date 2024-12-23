package main.java;

import java.io.File;
import java.net.InetAddress;
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
   * @param availableFilesPath: Path to the files to make available
   */
  public Daemon(String availableFilesPath) throws RemoteException {
    File availableFilesDir = new File(availableFilesPath);
    availableFiles = availableFilesDir.listFiles();
    currentDownloads = new HashMap<>();
    try {
      // Defaults to localhost
      String local = InetAddress.getLocalHost().getHostAddress();
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
  public void notifyDiary() {
    System.out.println(
        "Notifying Diary: " + String.join(":", "//" + diaryAddress, diaryPort.toString()) + diaryRegisterEndpoint);

    try {
      DiaryDaemon register = (DiaryDaemon) Naming
          .lookup(String.join(":", "//" + diaryAddress, diaryPort.toString()) + diaryRegisterEndpoint);

      if (availableFiles == null || availableFiles.length == 0) {
        throw new RuntimeException("No file in this directory");
      }
      for (File f : availableFiles) {
        if (f.isFile()) {
          System.out.println("Registering: " + f.getName());
          register.registerFile(daemonAddress, daemonPort, f.getName(), f.length());
        }
      }
    } catch (RuntimeException ae) {
      System.out.println("Failed to register to diary: " + ae.getMessage());
      System.exit(-1);
    } catch (Exception ae) {
      System.out.println("Failed to register to diary: " + ae);
      System.exit(-1);
    }
  }

  @Override
  public int download(String address, String filename, long offset, long size) throws RemoteException {

    int port = daemonPort + 1;
    System.out.println("Allocated port " + port + " for " + address);
    System.out.println("Sending " + filename);
    System.out.println("Chunk size " + size);
    System.out.println("Chunk offset " + offset);

    File file = null;
    for (File f : availableFiles) {
      String[] split = f.getPath().split("/");
      String name = split[split.length - 1];
      if (name.equals(filename)) {
        file = f;
        break;
      }
    }

    if (file == null) {
      System.out.println("File not available"); // TODO:should throw exception
    } else {
      Sender sender = new Sender(file, address, port, offset, size);
      sender.start();
    }

    return port;
  }

  public void listen() {
    try {
      try {
        LocateRegistry.createRegistry(daemonPort);
      } catch (RemoteException e) {
        LocateRegistry.getRegistry(daemonPort);
      }
      String URL = "//" + daemonAddress + ":" + daemonPort + "/download";
      Naming.rebind(URL, (FileProvider) this);
      System.out.println("Listening to requests");
    } catch (Exception e) {
      System.err.println("Server exception: " + e.toString());
      e.printStackTrace();
    }
  }

}
