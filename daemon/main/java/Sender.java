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
    while (!success) {
      try (Socket socket = new Socket(address, port)) {

        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        RandomAccessFile randomAccessFile = new RandomAccessFile(file.getAbsolutePath(), "r");
        var fileInputStream = randomAccessFile.getChannel();

        System.out.println("Sending " + size + " bytes");

        int bytes = 0;
        int bytesTotal = 0;
        // Here we break file into chunks
        // byte[] buffer = new byte[4 * 1024];
        ByteBuffer buffer = ByteBuffer.allocate(4 * 1024);
        // fileInputStream.skipNBytes(offset);
        while (bytesTotal < size && (bytes = fileInputStream.read(buffer, offset + bytesTotal)) != -1) {
          if (bytes < 4 * 1024) {
            var old = buffer.array();
            buffer.clear();
            buffer.put(old, 0, bytes); // Add only the read data to the buffer
          }

          dataOutputStream.write(buffer.array(), 0, bytes);
          dataOutputStream.flush();
          bytesTotal += bytes;
        }
        fileInputStream.close();
        dataOutputStream.close();
        fileInputStream.close();
        randomAccessFile.close();
        socket.close();
        success = true;

        System.out.println("Sent");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }
}
