package main.java;

import java.rmi.Naming;

public class AliveNotifyer implements Runnable {
    
    private final String stubUrl;
    private Daemon daemon;
    private boolean stillUsed;

	final String diaryStillAliveEndpoint = "/notifyalive";


    public AliveNotifyer(Daemon d, String url) {
        this.stubUrl = url;
        this.daemon = d;
        this.stillUsed = true;
    }

    @Override
    public void run() {
        try {
			DiaryDaemon register = (DiaryDaemon) Naming.lookup(stubUrl);
            while (stillUsed) {
                stillUsed = register.notifyAlive(daemon.daemonAddress, daemon.daemonPort);
                if (stillUsed) {
                    Thread.sleep(60_000);
                }
            }
            daemon.disconnect();
		} catch (RuntimeException ae) {
			System.out.println("Failed to register to diary: " + ae.getMessage());
			System.exit(-1);
		} catch (Exception ae) {
			System.out.println("Failed to register to diary: " + ae);
			System.exit(-1);
		}
    }

    public boolean getStillUsed() {
        return stillUsed;
    }
    
}
