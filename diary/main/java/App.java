package main.java;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class App {
  public static int default_port = 8081;

  public static Options create_options() {
    Options options = new Options();

    Option help = new Option("h", "help", false, "Print this help message");
    Option debug = new Option("d", "debug", false, "Print debug messages");

    Option diaryPortOpt = new Option("p", "port", true, "Port to use for the diary");

    options.addOption(help);
    options.addOption(debug);
    options.addOption(diaryPortOpt);
    return options;
  }

  static void exit_with_error(String error) {
    System.err.println(error);
    System.exit(-1);

  }

  public static CommandLine handle_cli(String[] args) throws ParseException {
    Options options = create_options();

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    return cmd;
  }

  public static void main(String[] args) {
    Logger logger = java.util.logging.Logger.getLogger("Diary");

    CommandLine cmd = null;
    try {
      cmd = handle_cli(args);
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
    int port = default_port;
    if (cmd.hasOption("p")) {
      try {
        port = Integer.parseInt(cmd.getOptionValue("p"));
      } catch (NumberFormatException e) {
        exit_with_error(e.toString());
      }
    }

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
        exit_with_error("Could not get or create registry: " + e.toString());
      }
    }

    DiaryImpl diary = null;
    try {
      diary = new DiaryImpl();
      diary.setLogger(logger);
    } catch (RemoteException e) {
      exit_with_error("Couldn't initialize Diary: " + e.toString());
    }
    
    try {
      String URL = "//" + InetAddress.getLocalHost().getHostAddress() + ":" + port + "/register";
      // Register the object with the naming service
      Naming.rebind(URL, (DiaryDaemon) diary);

      logger.info("Diary bound in registry Daemon");


      URL = "//" + InetAddress.getLocalHost().getHostAddress() + ":" + port + "/request";
      // Register the object with the naming service
      Naming.rebind(URL, (DiaryDownloader) diary);
      logger.info("Diary bound in registry Downloader");
    } catch (Exception e) {
      exit_with_error("Server exception: " + e.toString());
    }
  }
}
