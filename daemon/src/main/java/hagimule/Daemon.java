package hagimule;

import java.io.File;
import java.rmi.RemoteException;

/**
 * Daemon that registers available files and answers Download requests.
 */
public class Daemon implements FileProvider {

  public Daemon(String available_files_path) {

    System.out.println("Listing files from: " + available_files_path);

    File available_files_dir = new File(available_files_path);

    var available_files = available_files_dir.listFiles();
    for (var f : available_files) {
      System.out.println(f);
    }
  }

  public void notify_diary() {
    System.out.println("Notifying Diary");
  }

  @Override
  public void Download(String filename) throws RemoteException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'Download'");
  }
}
