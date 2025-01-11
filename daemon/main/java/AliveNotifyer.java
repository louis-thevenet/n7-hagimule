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
        Thread.sleep(60_000);
        System.out.println("Send notification alive");
        stillUsed = register.notifyAlive(daemon.daemonAddress, daemon.daemonPort);
      }
      daemon.disconnect();
    } catch (RuntimeException ae) {
      System.out.println("Failed to notify diary 5: " + ae.getMessage());
    } catch (Exception ae) {
      System.out.println("Failed to notify diary 6: " + ae);
    }
  }

  public boolean getStillUsed() {
    return stillUsed;
  }

}
