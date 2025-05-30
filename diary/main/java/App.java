package main.java;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** App launch a Diary and handle the command line. */
public class App {
  /** The default port for a diary. */
  public static final int defaultPort = 8081;

  /**
   * Create all the options menu for the CLI (Command Line Interface).
   *
   * @return the options menu.
   */
  public static Options create_options() {
    Options options = new Options();

    Option help = new Option("h", "help", false, "Print this help message");
    Option debug = new Option("d", "debug", false, "Print debug messages");

    Option diaryIpOpt = new Option("i", "ip", true, "address to use for the diary");
    Option diaryPortOpt = new Option("p", "port", true, "Port to use for the diary");

    options.addOption(help);
    options.addOption(debug);
    options.addOption(diaryIpOpt);
    options.addOption(diaryPortOpt);
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
  public static CommandLine handleCLI(String[] args) throws ParseException {
    Options options = create_options();

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    return cmd;
  }

  /**
   * Main method of the Class.
   *
   * @param args args of the command line
   */
  public static void main(String[] args) {
    // create a logger
    Logger logger = java.util.logging.Logger.getLogger("Diary");

    // handle command line
    CommandLine cmd = null;
    try {
      cmd = handleCLI(args);
    } catch (ParseException exp) {
      logger.severe("Parsing failed.  Reason: " + exp.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("diary", create_options());
      System.exit(-1);
    }

    // Print help message
    if (cmd.hasOption("help")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("diary", create_options());
      return;
    }

    // define the port to use
    int port = defaultPort;
    if (cmd.hasOption("p")) {
      try {
        port = Integer.parseInt(cmd.getOptionValue("p"));
      } catch (NumberFormatException e) {
        exitWithError(e.toString());
      }
    }

    // define the debug mode
    if (cmd.hasOption("d")) {
      logger.setLevel(Level.ALL);
    }

    // launching naming service
    try {
      LocateRegistry.createRegistry(port);
    } catch (RemoteException e) {
      try {
        LocateRegistry.getRegistry(port);
      } catch (RemoteException e1) {
        exitWithError("Could not get or create registry: " + e.toString());
      }
    }

    // launch a Diary
    DiaryImpl diary = null;
    try {
      diary = new DiaryImpl();
      diary.setLogger(logger);

      // set address
      if (cmd.hasOption("i")) {
        String address = cmd.getOptionValue("i");
        diary.setAddress(address);
      }
    } catch (RemoteException e) {
      exitWithError("Couldn't initialize Diary: " + e.toString());
    }

    try {
      System.setProperty("jave.rmi.server.hostname", diary.address);
      String URL = "//" + diary.address + ":" + port + "/register";

      // Register the object with the naming service to register file.
      Naming.rebind(URL, (DiaryDaemon) diary);
      logger.info("Diary bound in registry Daemon: " + URL);

      // Register the object with the naming service to disconnect a daemon
      URL = "//" + diary.address + ":" + port + "/disconnect";
      Naming.rebind(URL, (DiaryDaemon) diary);
      logger.info("Diary bound in registry Daemon: " + URL);

      // Register the object with the naming service to notify a Daemon is alive
      URL = "//" + diary.address + ":" + port + "/notify-alive";
      Naming.rebind(URL, (DiaryDaemon) diary);
      logger.info("Diary bound in registry Daemon: " + URL);

      // Register the object with the naming service to request a file.
      URL = "//" + diary.address + ":" + port + "/request";
      Naming.rebind(URL, (DiaryDownloader) diary);
      logger.info("Diary bound in registry Downloader: " + URL);

      logger.info("Start listening ...");
    } catch (Exception e) {
      exitWithError("Server exception: " + e.toString());
    }
  }
}
