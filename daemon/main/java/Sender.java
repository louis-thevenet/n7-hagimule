package main.java;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

public class Sender extends Thread {
  File file;
  String address;
  Integer port;

  public Sender(File file, String address, Integer port) {
    this.file = file;
    this.address = address;
    this.port = port;

  }

  public void run() {

    System.out.println("Sending " + file.getName() + " to " + address + ":" + port);

    try (Socket socket = new Socket(address, port)) {

      DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

      int bytes = 0;

      FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
      // Here we send the File to Server
      dataOutputStream.writeLong(file.length());

      // Here we break file into chunks
      byte[] buffer = new byte[4 * 1024];
      while ((bytes = fileInputStream.read(buffer)) != -1) {
        // Send the file to Server Socket
        dataOutputStream.write(buffer, 0, bytes);
        dataOutputStream.flush();
      }
      fileInputStream.close();
      dataOutputStream.close();
      fileInputStream.close();
      socket.close();

      System.out.println("Sent");
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
