package hagimule.downloader;

import java.util.HashMap;

public class Downloader implements Runnable {

  String file;
  String filename;
  Host host;
  int size;
  int part;
  int offset;
  HashMap<Integer, String> results;

  public Downloader(Host h, String filename, int size, int part, HashMap<Integer, String> results) {
    this.host = h;
    this.filename = filename;
    this.size = size;
    this.part = part;
    this.offset = part * size;
    this.results = results;
  }

  public void run() {
    // connect to the Host
    FileProvider fp = (FileProvider) Naming.lookup(h.getIp());
    System.err.println("Client exception: " + e.toString());
    e.printStackTrace();

    // download the file
    fp.download(filename, size, offset);
  }
}
