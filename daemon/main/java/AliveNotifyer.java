package main.java;

import java.rmi.Naming;

/**
 * AliveNotifyer is a thread which notify the diary of the health level of the daemon. And stop the
 * deamon if the diary has disconnected it.
 */
public class AliveNotifyer implements Runnable {

  /** The url of the stub. */
  private final String stubUrl;

  /** The daemon inspected. */
  private Daemon daemon;

  /** If the diary still use the daemon. */
  private boolean stillUsed;

  /**
   * Builder with the Deamon and the url.
   *
   * @param d the deamon.
   * @param url the url of the stub.
   */
  public AliveNotifyer(Daemon d, String url) {
    this.stubUrl = url;
    this.daemon = d;
    this.stillUsed = true;
  }

  /** Procedure run to notify the diary. */
  @Override
  public void run() {
    try {
      // connection to the stub
      System.out.println("Connect to : " + stubUrl);
      DiaryDaemon register = (DiaryDaemon) Naming.lookup(stubUrl);

      // notify the diary all the 80 s
      while (stillUsed) {
        // send notification
        System.out.println("Send notification alive");
        stillUsed = register.notifyAlive(daemon.getDaemonAddress(), daemon.getDaemonPort());

        // wait 80 s
        Thread.sleep(80_000);
      }

      // here the deamon is not use by the diary and will not be anymore so we shutdown it
      System.out.println("Shutdown daemon start");
      daemon.shutdown(false);
    } catch (InterruptedException ae) {
      // The thread is interrupted when the app is shutdown
      System.out.println("Shutdown Notifyer");

    } catch (RuntimeException ae) {
      System.out.println("Failed to notify diary with a RuntimeException: " + ae);

    } catch (Exception ae) {
      System.out.println("Failed to notify diary Exception: " + ae);
    }
  }

  /**
   * Get the sitllUsed Variable
   *
   * @return stillUsed
   */
  public boolean getStillUsed() {
    return stillUsed;
  }
}
