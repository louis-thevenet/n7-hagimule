package main.java;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

public class Downloader implements Runnable {
  String diaryAddress;

  public void setDiaryAddress(String diaryAddress) {
    this.diaryAddress = diaryAddress;
  }

  public void setDiaryPort(Integer diaryPort) {
    this.diaryPort = diaryPort;
  }

  Integer diaryPort = 8081;
  final String diaryRequestEndpoint = "/request";

  public Downloader() {
    try {
      // Defaults to localhost
      String local = "//" + InetAddress.getLocalHost().getHostAddress();
      diaryAddress = local;
    } catch (UnknownHostException e) {
      System.err.println("Could not retrieve local address");
    }
  }

  public void run() {
    // connect to the Host
    /*
     * FileProvider fp = (FileProvider) Naming.lookup(h.getIp());
     * System.err.println("Client exception: " + e.toString());
     * e.printStackTrace();
     * // download the file
     * fp.download(filename, size, offset);
     */
  }

  public void download(String filename) {
    List<Host> hosts = null;
    System.out.println("Requesting host list for: " + filename);
    try {
      DiaryDownloader stub = (DiaryDownloader) Naming
          .lookup(String.join(":", diaryAddress, diaryPort.toString()) + diaryRequestEndpoint);

      hosts = stub.request(filename);

    } catch (MalformedURLException | RemoteException | NotBoundException e) {
      System.err.println("Could not retrieve file list from diary: " + e);
      e.printStackTrace();
    } catch (FileIsNotAvailableException e) {

      System.err.println("File is not available at the moment: " + e);
      e.printStackTrace();
    }

    System.out.println("Available hosts:");
    for (Host h : hosts) {
      System.out.println(h.getIp());
      try {
        FileProvider stub = (FileProvider) Naming
            .lookup(h.getIp());
        stub.Download(filename);
      } catch (MalformedURLException | RemoteException | NotBoundException e) {
        System.err.println("Could not retrieve FileProvider RMI: " + e);
      }
    }

  }

  public void listFiles() {
    System.out.println("Available files:");

    try {
      DiaryDownloader stub = (DiaryDownloader) Naming
          .lookup(String.join(":", diaryAddress, diaryPort.toString()) + diaryRequestEndpoint);

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
