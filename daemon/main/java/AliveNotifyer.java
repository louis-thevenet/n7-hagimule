package main.java;

import java.rmi.Naming;

public class AliveNotifyer implements Runnable {

  private final String stubUrl;
  private Daemon daemon;
  private boolean stillUsed;

  public AliveNotifyer(Daemon d, String url) {
    this.stubUrl = url;
    this.daemon = d;
    this.stillUsed = true;
  }

  @Override
  public void run() {
    try {
      System.out.println("Connect to : " + stubUrl);
      DiaryDaemon register = (DiaryDaemon) Naming.lookup(stubUrl);
      while (stillUsed) {
        Thread.sleep(80_000);
        System.out.println("Send notification alive");
        stillUsed = register.notifyAlive(daemon.getDaemonAddress(), daemon.getDaemonPort());
      }
      System.out.println("Start daemon.disconnect()");
      daemon.shutdown(false);
    } catch (InterruptedException ae) {
      System.out.println("Shutdown Notifyer");
    } catch (RuntimeException ae) {
      System.out.println("Failed to notify diary Runtime: " + ae);
      ae.printStackTrace();
    } catch (Exception ae) {
      System.out.println("Failed to notify diary Exception: " + ae);
    }
  }

  public boolean getStillUsed() {
    return stillUsed;
  }

}
