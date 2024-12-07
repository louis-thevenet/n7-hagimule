package main.java;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;

/** Daemon that registers available files and answers Download requests. */
public class Daemon implements FileProvider {
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
  public Daemon(String available_files_path) {
    File availableFilesDir = new File(available_files_path);
    availableFiles = availableFilesDir.listFiles();

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
    String daemonDownloadAddress = String.join(":", daemonAddress, daemonPort.toString()) + daemonDownloadEndpoint;

    try {
      DiaryDaemon register = (DiaryDaemon) Naming
          .lookup(String.join(":", diaryAddress, diaryPort.toString()) + diaryRegisterEndpoint);

      for (File f : availableFiles) {
        System.out.println("Registering: " + f.getName());
        register.registerFile(daemonDownloadAddress, f.getName());
      }
    } catch (Exception ae) {
      System.out.println("Failed to register to diary: " + ae);
    }
  }

  @Override
  public void Download(String filename) throws RemoteException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'Download'");
  }
}
