package main.java;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

public class Downloader {
  String diaryAddress;
  String downloadPath;

  public void setDownloadPath(String downloadPath) {
    this.downloadPath = downloadPath;
  }

  public void setDiaryAddress(String diaryAddress) {
    this.diaryAddress = diaryAddress;
  }

  public void setDiaryPort(Integer diaryPort) {
    this.diaryPort = diaryPort;
  }

  Integer diaryPort = 8081;
  final String diaryRequestEndpoint = "request";

  public Downloader() {
    try {
      // Defaults to localhost
      String local = "//" + InetAddress.getLocalHost().getHostAddress();
      diaryAddress = local;
      String home = System.getProperty("user.home");
      downloadPath = home + "/Downloads";
    } catch (UnknownHostException e) {
      System.err.println("Could not retrieve local address");
    }
  }

  public void download(String filename) {
    // Get host list from Diary
    List<Host> hosts = null;
    System.out.println("Requesting host list for: " + filename);
    try {
      DiaryDownloader stub = (DiaryDownloader) Naming
          .lookup("//" + diaryAddress + ":" + diaryPort.toString() + "/" + diaryRequestEndpoint);

      hosts = stub.request(filename);

    } catch (MalformedURLException | RemoteException | NotBoundException e) {
      System.err.println("Could not retrieve file list from diary: " + e);
      e.printStackTrace();
    } catch (FileIsNotAvailableException e) {

      System.err.println("File is not available at the moment: " + e);
      e.printStackTrace();
    }

    // Download from hosts
    System.out.println("Available hosts:");
    for (Host h : hosts) {
      System.out.println(h.getIp());
      try {
        // Request a download port from a host
        FileProvider stub = (FileProvider) Naming
            .lookup("//" + h.getIp() + ':' + h.getPort() + "/download");

        String local = InetAddress.getLocalHost().getHostAddress();
        Integer tcpPort = stub.download(local, filename);

        ServerSocket serverSocket = new ServerSocket(tcpPort);
        Socket socket = serverSocket.accept();
        System.out
            .println("Successfully connected to host " + local + ":" + tcpPort + ". Downloading file " + filename);

        System.out.println(h.getIp().replaceAll("/", "") + ':' + tcpPort);

        DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(downloadPath + "/" + filename);

        long size = in.readLong(); // read file size
        byte[] buffer = new byte[4 * 1024];
        while (size > 0
            && (bytes = in.read(
                buffer, 0,
                (int) Math.min(buffer.length, size))) != -1) {
          // Here we write the file using write method
          fileOutputStream.write(buffer, 0, bytes);
          size -= bytes; // read upto file size
        }

        System.out.println("Wrote file to " + downloadPath + "/" + filename);
        fileOutputStream.close();
        in.close();
        socket.close();
        serverSocket.close();

      } catch (MalformedURLException | RemoteException | NotBoundException e) {
        System.err.println("Could not retrieve FileProvider RMI: " + e);
      } catch (UnknownHostException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

  }

  public void listFiles() {
    System.out.println("Available files:");

    try {
      DiaryDownloader stub = (DiaryDownloader) Naming
          .lookup("//" + diaryAddress + ":" + diaryPort + "/" + diaryRequestEndpoint);

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
