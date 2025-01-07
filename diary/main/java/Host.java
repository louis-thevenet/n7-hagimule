package main.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Host implements Serializable {

  private String ip;
  private Integer port;
  private long lastAck;

  public Integer getPort() {
    return port;
  }

  private List<String> files;

  public Host(String ip, Integer port) {
    if (ip == null || ip.length() == 0) {
      throw new IllegalArgumentException("ip wrong format");
    }
    this.ip = ip;
    this.port = port;
    this.files = new ArrayList<>();
    this.lastAck = System.currentTimeMillis();
  }

  public Host(String ip, List<String> files) {
    if (ip == null || files == null) {
      throw new IllegalArgumentException("args failed");
    }
    this.ip = ip;
    this.files = files;
  }

  public String toString() {
    return ip + port;
  }

  public void resetTime() {
    this.lastAck = System.currentTimeMillis();
  }

  public String getIp() {
    return ip;
  }

  public List<String> getFiles() {
    return files;
  }

  public long getTime() {
    return lastAck;
  }

  public void addFile(String nf) {
    if (nf == null || nf.length() == 0) {
      throw new IllegalArgumentException("args failed");
    }
    if (files.contains(nf)) {
      System.out.println("the file " + nf + " already exists.");
    } else {
      files.add(nf);
    }
  }

  @Override
  public boolean equals(Object obj) {
      Host h = (Host) obj;
      return h.ip.equals(this.ip) && h.port == port;
  }
}
