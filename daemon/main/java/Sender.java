package main.java;

import java.io.DataOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.Socket;

import java.nio.ByteBuffer;

public class Sender extends Thread {
  File file;
  String address;
  Integer port;
  private long offset;
  private long size;

  public Sender(File file, String address, Integer port, long offset, long size) {
    this.file = file;
    this.address = address;
    this.port = port;
    this.offset = offset;
    this.size = size;
  }

  public void run() {

    System.out.println("Sending " + file.getName() + " to " + address + ":" + port);
    boolean success = false;
    // while (!success) {
    try (Socket socket = new Socket(address, port)) {

      DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

      RandomAccessFile randomAccessFile = new RandomAccessFile(file.getAbsolutePath(), "r");
      var fileInputStream = randomAccessFile.getChannel();

      System.out.println("Sending a total of " + size + " bytes");

      int bytes = 0;
      int bytesTotal = 0;
      int bufferSize = 64 * 1024;
      ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
      while (bytesTotal <= size && (bytes = fileInputStream.read(buffer, offset + bytesTotal)) != -1) {
        buffer.flip();
        int to_send = Math.min((int) size - bytesTotal, Math.min(bytes, (int) size));
        dataOutputStream.write(buffer.array(), 0, to_send);
        System.out.println("Sent " + to_send + " bytes"
            +
            " from " + (offset + bytesTotal) + " to " + (offset + bytesTotal + to_send));
        dataOutputStream.flush();
        bytesTotal += bytes;
      }
      fileInputStream.close();
      dataOutputStream.close();
      fileInputStream.close();
      randomAccessFile.close();
      socket.close();
      // success = true;

      System.out.println("Sent");
    } catch (Exception e) {
      System.out.println("Failed to connect to downloader: " + e + "\n Retrying...");
    }
    // }

  }
}
