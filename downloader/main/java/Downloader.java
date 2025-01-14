package main.java;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Downloader request a file. */
public class Downloader {

  /** The diary ip address */
  private String diaryAddress;

  /** The path of the dir where the files has to be download. */
  private String downloadPath;

  /** The address of the downloader. */
  private String downloaderAddress;

  public void download(String filename) {
    // Get host list from Diary
    List<Host> hosts = new ArrayList<>();
    System.out.println("Requesting host list for: " + filename);
    long sizeOfFile = 0;
    try {
      // connect
      DiaryDownloader stub =
          (DiaryDownloader)
              Naming.lookup(
                  "//" + diaryAddress + ":" + diaryPort.toString() + "/" + diaryRequestEndpoint);

      // get the the list.
      hosts = stub.request(filename);
      // get the size of the file
      sizeOfFile = stub.sizeOf(filename);

    } catch (MalformedURLException | RemoteException | NotBoundException e) {
      System.err.println("Could not retrieve file list from diary: " + e);
      e.printStackTrace();
    } catch (FileIsNotAvailableException e) {
      System.err.println("File is not available at the moment: " + e.getMessage());
    }

    // Download from hosts
    if (hosts.size() > 0) {

      System.out.println("Available hosts:");
      for (Host h : hosts) {
        System.out.println(h.getIp());
      }

      System.out.println("File size: " + sizeOfFile);

      // size of Download done by each source
      long taskSize = sizeOfFile / hosts.size();

      System.out.println("Split task in " + hosts.size() + " parts");
      System.out.println("Task size: " + taskSize);

      // Set of thread
      Set<Thread> threads = new HashSet<>();

      // Lists of activities
      List<InnerDownloader> jobs = new ArrayList<>();

      RandomAccessFile reader = null;
      try {
        reader = new RandomAccessFile(downloadPath + "/" + filename, "rw");
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      FileChannel channel = reader.getChannel();

      // Jobs creation
      int i = 0;
      for (Host h : hosts) {
        long offset = i * taskSize;
        if (i == hosts.size() - 1) {
          jobs.add(new InnerDownloader(h, i, filename, offset, sizeOfFile - offset, channel));
          System.out.println(
              "Host "
                  + h.getIp()
                  + " : "
                  + i
                  + " : "
                  + offset
                  + " -> "
                  + (offset + sizeOfFile - offset + 1));
        } else {

          jobs.add(new InnerDownloader(h, i, filename, offset, taskSize, channel));
          System.out.println(
              "Host " + h.getIp() + " : " + i + " : " + offset + " -> " + (offset + taskSize));
        }
        i++;
      }
      // Jobs start
      for (InnerDownloader d : jobs) {
        Thread t = new Thread(d);
        threads.add(t);
        t.start();
      }

      // wait for end
      for (Thread t : threads) {
        try {
          t.join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /** Inner Thread to download a part of a file */
  public class InnerDownloader implements Runnable {

    /** The host. */
    private Host h;

    /** The offset of the part. */
    private long offset;

    /** The job ID. */
    private int id;

    /** The size of the part. */
    private long size;

    /** The filename. */
    private String filename;

    /** The output. */
    private FileChannel output;

    /**
     * Builder of a InnerDownloader.
     *
     * @param h the host.
     * @param filename the filename.
     * @param offset the offset of the part of file.
     * @param size the size of the part.
     * @param output the output.
     */
    public InnerDownloader(
        Host h, int i, String filename, long offset, long size, FileChannel output) {
      this.h = h;
      this.offset = offset;
      this.size = size;
      this.output = output;
      this.filename = filename;
      this.id = i;
    }

    /** Procedure of the Thread. */
    @Override
    public void run() {
      long worker_id = this.id;
      String id_str = "[" + worker_id + "] ";
      int port = 45654 + (int) worker_id;
      try {
        // Request a download port from a host
        FileProvider stub =
            (FileProvider) Naming.lookup("//" + h.getIp() + ':' + h.getPort() + "/download");
        int tcpPort = stub.download(downloaderAddress, port, filename, offset, size);

        // set up the server socket
        ServerSocket serverSocket = new ServerSocket(port);
        Socket socket = serverSocket.accept();
        System.out.println(id_str + "Successfully connected to host " + h.getIp() + ":" + tcpPort);
        System.out.println(
            id_str + "Downloading from " + offset + " to " + (offset + size) + " of " + filename);

        // set up communication
        DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        // handle reception
        int bytes = 0;
        int bytesTotal = 0;
        int bufferSize = 64 * 1024;
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        while (bytesTotal <= size
            && (bytes = in.read(buffer.array(), 0, (int) Math.min(buffer.capacity(), size)))
                != -1) {

          if (bytes < bufferSize) {
            byte[] old = buffer.array();
            buffer.clear();
            buffer.put(old, 0, bytes); // Add only the read data to the buffer
          }

          // output.lock(offset + bytesTotal, bytes, true);
          buffer.flip();
          output.write(buffer, offset + bytesTotal);
          bytesTotal += bytes;
        }

        // close communication
        in.close();
        socket.close();
        serverSocket.close();

      } catch (MalformedURLException | RemoteException | NotBoundException e) {
        System.err.println("Could not retrieve FileProvider RMI: " + e);
      } catch (UnknownHostException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Set the downloader address.
   *
   * @param downloaderAddress the ip address.
   */
  public void setDownloaderAddress(String downloaderAddress) {
    this.downloaderAddress = downloaderAddress;
  }

  /**
   * Set the downloader path.
   *
   * @param downloadPath the path.
   */
  public void setDownloadPath(String downloadPath) {
    this.downloadPath = downloadPath;
  }

  /**
   * Set the diary
   *
   * @param diaryAddress the ip address.
   */
  public void setDiaryAddress(String diaryAddress) {
    this.diaryAddress = diaryAddress;
  }

  /**
   * Set the diary port
   *
   * @param diaryPort the port number.
   */
  public void setDiaryPort(Integer diaryPort) {
    this.diaryPort = diaryPort;
  }

  /** The diary port. */
  private Integer diaryPort = 8081;

  /** The end of the url of the stub. */
  private final String diaryRequestEndpoint = "request";

  /** Builder of a Downloader. */
  public Downloader() {
    try {
      // Defaults to localhost
      String local = InetAddress.getLocalHost().getHostAddress();
      diaryAddress = local;
      downloaderAddress = local;
      String home = System.getProperty("user.home");
      downloadPath = home + "/Downloads";
    } catch (UnknownHostException e) {
      System.err.println("Could not retrieve local address");
    }
  }

  /** List the files available in the diary in the prompt. */
  public void listFiles() {
    System.out.println(
        "Listing files from: "
            + "//"
            + diaryAddress
            + ":"
            + diaryPort
            + "/"
            + diaryRequestEndpoint);
    System.out.println("Available files:");

    try {
      DiaryDownloader stub =
          (DiaryDownloader)
              Naming.lookup("//" + diaryAddress + ":" + diaryPort + "/" + diaryRequestEndpoint);

      List<String> files = stub.listFiles();
      for (String f : files) {
        System.out.println("-> " + f);
      }
    } catch (MalformedURLException | RemoteException | NotBoundException e) {
      System.err.println("Could not retrieve file list from diary: " + e);
      e.printStackTrace();
    }
  }
}
