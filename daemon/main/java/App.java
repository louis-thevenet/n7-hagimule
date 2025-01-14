package main.java;

import java.rmi.RemoteException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Lanceur d'un daemon sur une application. Launch a daemon application. usage :
 * -h show options.
 * -dii <arg> define diary ip address. -dip <arg> define diary port. -dai <arg>
 * define daemon ip
 * address. -dap <arg> define daemon port. -p <arg> define path to include in
 * the download. -s show
 * a summary of the daemon variable
 */
public class App {

  /** Handler for SIGINT. Let the app close cleanly. */
  private static class ShutdownHook implements Runnable {

    /** Deamon of the App. */
    private Daemon d;

    /**
     * Builder with the deamon of the App.
     *
     * @param d the daemon.
     */
    private ShutdownHook(Daemon d) {
      this.d = d;
    }

    /** Procedure run when SIGINT is thrown. */
    @Override
    public void run() {
      d.shutdown(true);
      System.out.println("Shutdown App");
    }
  }

  /** Define the home dir of the linux user. */
  static String homeDir = System.getProperty("user.home");

  /** Define the default path where files will be register to diary. */
  static String defaultFilesPath = homeDir + "/Downloads/";

  /**
   * Create all the options menu for the CLI (Command Line Interface).
   *
   * @return the options menu.
   */
  static Options createOptions() {
    Options options = new Options();

    Option help = new Option("h", "help", false, "Print this help message");
    Option summaryOpt = new Option(
        "s",
        "summary",
        false,
        "Prints a summary of this daemon settings with current arguments");

    Option pathOpt = new Option("p", "path", true, "Path to files to make available");

    Option daemonAddressOpt = new Option("dai", "daemon-ip", true, "Address to use to receive download requests");
    Option daemonPortOpt = new Option("dap", "daemon-port", true, "Port to use to receive download requests");

    Option diaryAddressOpt = new Option("dii", "diary-ip", true, "Address to use to register files to the diary");
    Option diaryPortOpt = new Option("dip", "diary-port", true, "Port to use to register files to the diary");

    Option daemonBufferDelay = new Option("t", "buffer-delat", true, "Time to sleep between each buffer sent");

    options.addOption(help);
    options.addOption(pathOpt);
    options.addOption(summaryOpt);
    options.addOption(daemonAddressOpt);
    options.addOption(daemonPortOpt);
    options.addOption(diaryAddressOpt);
    options.addOption(diaryPortOpt);
    options.addOption(daemonBufferDelay);

    return options;
  }

  /**
   * Exit the App with an error.
   *
   * @param error the error message.
   */
  static void exitWithError(String error) {
    System.err.println(error);
    System.exit(-1);
  }

  /**
   * Handler of the CLI.
   *
   * @param args args of the command line.
   * @return A object Command line with the command line info parsed.
   * @throws ParseException if the parse failed.
   */
  static CommandLine handleCLI(String[] args) throws ParseException {
    Options options = createOptions();

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);
    return cmd;
  }

  /**
   * Get the file path function of the command line.
   *
   * @param cmd the command line.
   * @return the files path to register.
   */
  static String getFilesPath(CommandLine cmd) {
    String availableFilesPath;
    if (cmd.hasOption("path")) {
      availableFilesPath = cmd.getOptionValue("path");
    } else {
      availableFilesPath = defaultFilesPath;
    }
    return availableFilesPath;
  }

  /**
   * Create a Daemon with specification of the command line.
   *
   * @param cmd command line.
   * @return a Daemon with the specification.
   */
  static Daemon createDaemon(CommandLine cmd) {

    Daemon daemon = null;
    // create a Daemon
    try {
      daemon = new Daemon(getFilesPath(cmd));
    } catch (RemoteException e) {
      exitWithError("Failed to create daemon object: " + e);
    }

    // change daemon ip
    if (cmd.hasOption("dai")) {
      daemon.setDaemonAddress(cmd.getOptionValue("dai"));
    }

    // change daemon port
    if (cmd.hasOption("dap")) {
      try {
        int port = Integer.parseInt(cmd.getOptionValue("dap"));
        daemon.setDaemonPort(port);
      } catch (NumberFormatException e) {
        exitWithError(e.toString());
      }
    }

    // change the diary ip
    if (cmd.hasOption("dii")) {
      daemon.setDiaryAddress(cmd.getOptionValue("dii"));
    }

    // change the diary port
    if (cmd.hasOption("dip")) {
      try {
        int port = Integer.parseInt(cmd.getOptionValue("dip"));
        daemon.setDiaryPort(port);
      } catch (NumberFormatException e) {
        exitWithError(e.toString());
      }
    }
    // Set the sleep time

    if (cmd.hasOption("t")) {
      try {

        long time = Integer.parseInt(cmd.getOptionValue("t"));
        daemon.setBufferDelay(time);
      } catch (NumberFormatException e) {
        exitWithError(e.toString());
      }
    }

    // show summary and stop
    if (cmd.hasOption("s")) {
      daemon.makeSummary();
      System.exit(0);
    }
    return daemon;
  }

  /**
   * Main method of the Class.
   *
   * @param args args of the command line
   */
  public static void main(String[] args) {
    CommandLine cmd = null;
    try {
      cmd = handleCLI(args);
      // Print help message
      if (cmd.hasOption("help")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("daemon", createOptions());
        return;
      }
    } catch (ParseException exp) {
      System.err.println("Parsing failed.  Reason: " + exp.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("daemon", createOptions());
      System.exit(-1);
    }

    Daemon daemon = createDaemon(cmd);

    // Set up the Clean Shutdown
    Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(daemon)));

    // register files into diary
    daemon.notifyDiary();

    // listen for request
    daemon.listen();

    // start to notify the diary every minutes
    daemon.startNotifying();
  }
}
