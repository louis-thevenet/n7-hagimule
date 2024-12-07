package hagimule.diary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Host implements Serializable {

  private String ip;
  private List<String> files;

  public Host(String ip) {
    if (ip == null || ip.length() == 0) {
      throw new IllegalArgumentException("ip wrong format");
    }
    this.ip = ip;
    this.files = new ArrayList<>();
  }

  public Host(String ip, List<String> files) {
    if (ip == null || files == null) {
      throw new IllegalArgumentException("args failed");
    }
    this.ip = ip;
    this.files = files;
  }

  public String toString() {
    return ip;
  }

  public String getIp() {
    return ip;
  }

  public List<String> getFiles() {
    return files;
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
}
