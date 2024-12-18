package main.java;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;


public class Downloader {
    String diaryAddress;
    String downloadPath;
    String downloaderAddress;

    public void download(String filename) {
        // Get host list from Diary
        List<Host> hosts = new ArrayList<>();
        System.out.println("Requesting host list for: " + filename);
        long sizeOfFile = 0;
        try {
            DiaryDownloader stub = (DiaryDownloader) Naming
                    .lookup("//" + diaryAddress + ":" + diaryPort.toString() + "/" + diaryRequestEndpoint);

            hosts = stub.request(filename);
            sizeOfFile = stub.sizeOf(filename);

        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            System.err.println("Could not retrieve file list from diary: " + e);
            e.printStackTrace();
        } catch (FileIsNotAvailableException e) {
            System.err.println("File is not available at the moment: " + e.getMessage());
        }

        // Download from hosts
        if (hosts.size() > 0) {
            System.out.println("Available hosts:");

            // size of Download done by each source
            long taskSize = sizeOfFile / hosts.size();

            // Set of thread
            Set<Thread> threads = new HashSet<>();

            // List of results
            ByteArrayOutputStream results = new ByteArrayOutputStream((int) sizeOfFile);

            // Lists of activities
            List<InnerDownloader> jobs = new ArrayList<>();

            // Jobs creation
            int i = 0;
            for (Host h : hosts) {
                jobs.add(new InnerDownloader(h, filename, taskSize, i, results));
                i++;
            }

            // Jobs start
            for (InnerDownloader d : jobs) {
                Thread t = new Thread(d);
                threads.add(t);
                t.start();
            }


            // wait for end
            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // // results combination
            // String res = "";
            // for (int i = 0; i < lh.length; i++) {
            //     res.concat(results.get(i));
            // }

            try {
                OutputStream outputStream = new FileOutputStream(filename);
                results.writeTo(outputStream);
            } catch (Exception e) {
                throw new RuntimeException("Le fichier n'a pas pu être crée");
            }
        }
    }

    public class InnerDownloader implements Runnable {

        private Host h;
        private long offset;
        private long size;
        private String filename;
        private ByteArrayOutputStream results;

        public InnerDownloader(Host h, String filename, long offset, long size, ByteArrayOutputStream results) {
            this.h = h;
            this.offset = offset;
            this.size = size;
            this.results = results;
            this.filename = filename;
        }

        public void run() {
            System.out.println("//" + h.getIp() + ':' + h.getPort() + "/download");
            try {
                // Request a download port from a host
                FileProvider stub = (FileProvider) Naming
                        .lookup("//" + h.getIp() + ':' + h.getPort() + "/download");

                Integer tcpPort = stub.download(downloaderAddress, filename, offset, size);

                ServerSocket serverSocket = new ServerSocket(tcpPort);
                Socket socket = serverSocket.accept();
                System.out
                        .println("Successfully connected to host " + downloaderAddress + ":" + tcpPort
                                + ". Downloading file "
                                + filename);

                System.out.println(h.getIp().replaceAll("/", "") + ':' + tcpPort);

                DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                int bytes = 0;

                long size = in.readLong(); // read file size
                byte[] buffer = new byte[4 * 1024];
                while (size > 0
                        && (bytes = in.read(
                                buffer, 0,
                                (int) Math.min(buffer.length, size))) != -1) {
                    // Here we write the file using write method
                    results.write(buffer, 0, bytes);

                    size -= bytes; // read upto file size
                }

                System.out.println("Wrote file to " + downloadPath + "/" + filename);
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

    public void setDownloaderAddress(String downloaderAddress) {
        this.downloaderAddress = downloaderAddress;
    }

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
            downloaderAddress = local;
            String home = System.getProperty("user.home");
            downloadPath = home + "/Downloads";
        } catch (UnknownHostException e) {
            System.err.println("Could not retrieve local address");
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
