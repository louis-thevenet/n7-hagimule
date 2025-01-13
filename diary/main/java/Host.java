package main.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Host is a ip address, a port and a list of file provided. */
public class Host implements Serializable {

  /** The ip address of the host. */
  private String ip;

  /** The port of the host. */
  private Integer port;

  /** The time of the last. */
  private long lastAck;

  /**
   * Get the port of the host.
   *
   * @return the port.
   */
  public Integer getPort() {
    return port;
  }

  /** The list of file of the host. */
  private List<String> files;

  /**
   * Builder of a host.
   *
   * @param ip the ip of the host.
   * @param port the port of the host.
   */
  public Host(String ip, Integer port) {
    if (ip == null || ip.length() == 0) {
      throw new IllegalArgumentException("ip wrong format");
    }
    this.ip = ip;
    this.port = port;
    this.files = new ArrayList<>();
    this.lastAck = System.currentTimeMillis();
  }

  /** Way to print a Host. */
  @Override
  public String toString() {
    return ip + ":" + port;
  }

  /** Reset the time of the last alive notification. */
  public void resetTime() {
    this.lastAck = System.currentTimeMillis();
  }

  /**
   * Get the ip address.
   *
   * @return the ip address of the Host.
   */
  public String getIp() {
    return ip;
  }

  /**
   * Get the list of files provided by the host.
   *
   * @return the list of files.
   */
  public List<String> getFiles() {
    return files;
  }

  /**
   * Get the last ack time.
   *
   * @return the time in ms.
   */
  public long getTime() {
    return lastAck;
  }

  /**
   * Add a file to the list of file.
   *
   * @param nf the new filename.
   */
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
    return h.ip.equals(this.ip) && h.port.equals(port);
  }
}
