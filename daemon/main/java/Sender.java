package main.java;

import java.io.DataOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Sender is a Thread charged to send a part of file by a Socket to the
 * serverSocket gifted in the
 * builder.
 */
public class Sender extends Thread {
  /** The file to send. */
  private File file;

  /** The ip address of the downloader. */
  private String address;

  /** The port of the downloader where there is a serverSocket listening. */
  private Integer port;

  /** The offset of the part of file. */
  private long offset;

  /** The size of the part of file to send. */
  private long size;

  /** The number file send. */
  private Integer fileCSend;

  /** The time to sleep between each buffer sent. */
  private long bufferDelay;

  /**
   * Builder of a Sender.
   *
   * @param file        the file to send.
   * @param address     the ip address of the downloader.
   * @param port        the port of the downloader.
   * @param offset      the offset of the part of file to send.
   * @param size        the size of the part of file to send.
   * @param fileCSend   the object of the number of file currently send.
   * @param bufferDelay the time to sleep between each buffer sent.
   */
  public Sender(
      File file, String address, Integer port, long offset, long size, Integer fileCSend, long bufferDelay) {
    this.file = file;
    this.address = address;
    this.port = port;
    this.offset = offset;
    this.size = size;
    this.fileCSend = fileCSend;
    this.bufferDelay = bufferDelay;
  }

  /** Procedure of the Thread. */
  @Override
  public void run() {
    // increment the numbre of file send
    fileCSend++;
    System.out.println("Sending " + file.getName() + " to " + address + ":" + port);
    boolean success = false;
    // while the daemon can not access to the serverSocket
    while (!success) {

      try (Socket socket = new Socket(address, port)) {

        // set up the communication with a dataOuputStream
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        // access to the file
        RandomAccessFile randomAccessFile = new RandomAccessFile(file.getAbsolutePath(), "r");

        // set up the communication with a FileChannel
        FileChannel fileInputStream = randomAccessFile.getChannel();

        System.out.println("Sending a total of " + size + " bytes");

        // send the bytes
        int bytes = 0;
        int bytesTotal = 0;
        int bufferSize = 64 * 1024;
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        while (bytesTotal <= size
            && (bytes = fileInputStream.read(buffer, offset + bytesTotal)) != -1) {
          Thread.sleep(this.bufferDelay);
          buffer.flip();
          int to_send = Math.min((int) size - bytesTotal, Math.min(bytes, (int) size));
          dataOutputStream.write(buffer.array(), 0, to_send);
          // System.out.println("Sent " + to_send + " bytes"
          // +
          // " from " + (offset + bytesTotal) + " to " + (offset + bytesTotal + to_send));
          dataOutputStream.flush();
          bytesTotal += bytes;
        }

        // close communication
        fileInputStream.close();
        dataOutputStream.close();
        fileInputStream.close();
        randomAccessFile.close();
        socket.close();
        success = true;

        System.out.println("Sent");
      } catch (Exception e) {
        System.out.println("Failed to connect to downloader: " + e + "\n Retrying...");
      }
    }
    // decrement file send
    fileCSend--;
  }
}
