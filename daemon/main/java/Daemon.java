package main.java;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/** Daemon that registers available files and answers Download requests. */
public class Daemon extends UnicastRemoteObject implements FileProvider {

  /** List of files provided by the deamon. */
  private File[] availableFiles;

  /** File of the directory. */
  private File availableFilesDir;

  /** The ip address of the diary. */
  private String diaryAddress;

  /** The port used by the diary. */
  private Integer diaryPort = 8081;

  /** The number of files currently provided. */
  private Integer fileCurrentlySend = 0;

  /** The Notifyer to diary that the daemon is alive. */
  private AliveNotifyer notifyer;

  /** The thread of the notifyer. */
  private Thread thNotifyer;

  /** The time to sleep between each buffer sent. */
  private long bufferDelay = 0;

  public void setAvailableFiles(File[] availableFiles) {
    this.availableFiles = availableFiles;
  }

  public void setFileCurrentlySend(Integer fileCurrentlySend) {
    this.fileCurrentlySend = fileCurrentlySend;
  }

  public void setNotifyer(AliveNotifyer notifyer) {
    this.notifyer = notifyer;
  }

  public void setThNotifyer(Thread thNotifyer) {
    this.thNotifyer = thNotifyer;
  }

  public void setBufferDelay(long bufferDelay) {
    this.bufferDelay = bufferDelay;
  }

  /**
   * Set the diary adress.
   *
   * @param diaryAddress the new value
   */
  public void setDiaryAddress(String diaryAddress) {
    this.diaryAddress = diaryAddress;
  }

  /**
   * Set the diary port.
   *
   * @param diaryPort the new value.
   */
  public void setDiaryPort(Integer diaryPort) {
    this.diaryPort = diaryPort;
  }

  /**
   * Set the daemon ip address
   *
   * @param daemonAddress the new value.
   */
  public void setDaemonAddress(String daemonAddress) {
    this.daemonAddress = daemonAddress;
  }

  /**
   * Set the deamon port.
   *
   * @param daemonPort the new value
   */
  public void setDaemonPort(Integer daemonPort) {
    this.daemonPort = daemonPort;
  }

  /** The final extensions of the stub url to register. */
  final String diaryRegisterEndpoint = "/register";

  /** The final extensions of the stub url to disconnect. */
  private final String diaryDisconnectEndpoint = "/disconnect";

  /** The final extensions of the stub url to notify-alive. */
  private final String diaryStillAliveEndpoint = "/notify-alive";

  /** The daemon ip address. */
  private String daemonAddress;

  /** The daemon port integer. */
  private Integer daemonPort = 8082;

  /** The final extensions of the stub url to download. */
  final String daemonDownloadEndpoint = "/download";

  /**
   * Creates a new Daemon object.
   *
   * @param availableFilesPath: Path to the files to make available
   */
  public Daemon(String availableFilesPath) throws RemoteException {
    this.availableFilesDir = new File(availableFilesPath);
    availableFiles = availableFilesDir.listFiles();
    try {
      // Defaults to localhost
      String local = InetAddress.getLocalHost().getHostAddress();
      diaryAddress = local;
      daemonAddress = local;
    } catch (UnknownHostException e) {
      System.err.println("Could not retrieve local address");
    }
  }

  /** Prints a summary of this Daemon settings. */
  public void makeSummary() {
    System.out.println(
        "Diary address: "
            + String.join(":", diaryAddress, diaryPort.toString())
            + diaryRegisterEndpoint);
    System.out.println(
        "Daemon address: "
            + String.join(":", daemonAddress, daemonPort.toString())
            + daemonDownloadEndpoint);

    System.out.println("Files available:");
    for (File f : availableFiles) {
      System.out.println("- " + f.getName());
    }
  }

  /** Registers each files to be made available to the Diary. */
  public void notifyDiary() {
    System.out.println(
        "Notifying Diary: "
            + String.join(":", "//" + diaryAddress, diaryPort.toString())
            + diaryRegisterEndpoint);

    try {
      // connect the stub
      DiaryDaemon register = (DiaryDaemon) Naming.lookup(
          String.join(":", "//" + diaryAddress, diaryPort.toString())
              + diaryRegisterEndpoint);

      // register each file
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
      System.out.println("Failed to register to diary Runtime: " + ae);
      this.shutdown(true);
    } catch (Exception ae) {
      System.out.println("Failed to register to diary Exception: " + ae);
      this.shutdown(true);

    }
  }

  /** Registers each new files to be made available to the Diary. */
  public void notifyChange() {
    List<File> lf = new ArrayList<>();
    for (File f : availableFilesDir.listFiles()) {
      boolean found = false;
      for (File f2 : availableFiles) {
        if (f.getName().equals(f2.getName())) {
          found = true;
          break;
        }
      }
      if (!found) {
        lf.add(f);
      }
    }

    if (lf.size() > 0) {
      System.out.println(
          "Notifying Diary: "
              + String.join(":", "//" + diaryAddress, diaryPort.toString())
              + diaryRegisterEndpoint);
      try {
        // connect the stub
        DiaryDaemon register = (DiaryDaemon) Naming.lookup(
            String.join(":", "//" + diaryAddress, diaryPort.toString())
                + diaryRegisterEndpoint);

        // register each file
        for (File f : lf) {
          if (f.isFile()) {
            System.out.println("Registering: " + f.getName());
            register.registerFile(daemonAddress, daemonPort, f.getName(), f.length());
          }
        }
      } catch (RuntimeException ae) {
        System.out.println("Failed to register to diary Runtime: " + ae);
        this.shutdown(true);
      } catch (Exception ae) {
        System.out.println("Failed to register to diary Exception: " + ae);
        this.shutdown(true);
      }
    }
  }

  @Override
  public int download(
      String downloaderAddress, int downloaderPort, String filename, long offset, long size)
      throws RemoteException {
    // define the send port
    System.out.println("FCS = " + fileCurrentlySend);
    int port = daemonPort + 1 + fileCurrentlySend;
    System.out.println("Allocated port " + port + " for " + downloaderAddress);
    System.out.println("Sending " + filename);
    System.out.println("Chunk size " + size);
    System.out.println("Chunk offset " + offset);

    // find the file
    File file = null;
    for (File f : availableFiles) {
      String[] split = f.getPath().split("/");
      String name = split[split.length - 1];
      if (name.equals(filename)) {
        file = f;
        break;
      }
    }

    // check the existence of the file and launch the sender
    if (file == null) {
      throw new RemoteException("File is not available");
    } else {
      Sender sender = new Sender(file, downloaderAddress, downloaderPort, offset, size, fileCurrentlySend,
          this.bufferDelay);
      sender.start();
    }
    return port;
  }

  /** Start to listen request. */
  public void listen() {
    try {
      // open a listening port
      try {
        LocateRegistry.createRegistry(daemonPort);
      } catch (RemoteException e) {
        LocateRegistry.getRegistry(daemonPort);
      }
      String URL = "//" + daemonAddress + ":" + daemonPort + "/download";
      // bind the service into the register
      Naming.rebind(URL, (FileProvider) this);
      System.out.println("Listening to requests");
    } catch (Exception e) {
      System.err.println("Server exception: " + e.toString());
      e.printStackTrace();
    }
  }

  /**
   * Stop a deamon cleanly.
   *
   * @param thInterrupt if the notify thread has to be interrupt.
   */
  public void shutdown(boolean thInterrupt) {
    try {
      // send a notification to the diary that the deamon disconnect.
      DiaryDaemon register = (DiaryDaemon) Naming.lookup(
          String.join(":", "//" + diaryAddress, diaryPort.toString())
              + diaryDisconnectEndpoint);
      System.out.println("send disconnect notification");
      register.disconnect(daemonAddress, daemonPort);
      System.out.println("Shutdown Daemon");
    } catch (RuntimeException ae) {
      System.out.println("Failed to shutdown App Runtime: " + ae);
    } catch (Exception ae) {
      System.out.println("Failed to shutdown App Exception: " + ae);
    } finally {
      if (thInterrupt && thNotifyer != null) {
        thNotifyer.interrupt();
      }
    }
  }

  /** Start to notify the diary that the deamon is alive. */
  public void startNotifying() {
    // create the notifyer
    notifyer = new AliveNotifyer(
        this,
        String.join(":", "//" + diaryAddress, diaryPort.toString()) + diaryStillAliveEndpoint);
    thNotifyer = new Thread(notifyer);

    // start it
    thNotifyer.start();
  }

  /**
   * Get the Deamon ip address.
   *
   * @return the deamon ip address
   */
  public String getDaemonAddress() {
    return daemonAddress;
  }

  /**
   * Get the Daemon port.
   *
   * @return the deamon port.
   */
  public Integer getDaemonPort() {
    return daemonPort;
  }

  /**
   * Get the notifyer.
   *
   * @return the notifyer.
   */
  public AliveNotifyer getNotifyer() {
    return this.notifyer;
  }

  /**
   * Get the directory.
   * 
   * @return the directory.
   */
  public File getAvailableFilesDir() {
    return availableFilesDir;
  }

  /**
   * Get the list of files provided.
   * 
   * @return the lists.
   */
  public File[] getAvailableFiles() {
    return availableFiles;
  }
}
