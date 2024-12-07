package main.java;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class App {
  static String home_dir = System.getProperty("user.home");
  static String default_files_path = home_dir + "/Downloads/";

  static Options create_options() {
    Options options = new Options();

    Option help = new Option("h", "help", false, "Print this help message");
    Option summaryOpt = new Option("s", "summary", false,
        "Prints a summary of this daemon settings with current arguments");

    Option pathOpt = new Option("p", "path", true, "Path to files to make available");

    Option daemonAddressOpt = new Option("dai", "daemon-ip", true, "Address to use to receive download requests");
    Option daemonPortOpt = new Option("dap", "daemon-port", true, "Port to use to receive download requests");

    Option diaryAddressOpt = new Option("dii", "diary-ip", true, "Address to use to register files to the diary");
    Option diaryPortOpt = new Option("dip", "diary-port", true, "Port to use to register files to the diary");

    options.addOption(help);
    options.addOption(pathOpt);
    options.addOption(summaryOpt);
    options.addOption(daemonAddressOpt);
    options.addOption(daemonPortOpt);
    options.addOption(diaryAddressOpt);
    options.addOption(diaryPortOpt);

    return options;
  }

  static void exit_with_error(String error) {
    System.err.println(error);
    System.exit(-1);

  }

  static CommandLine handle_cli(String[] args) throws ParseException {
    Options options = create_options();

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);
    return cmd;
  }

  static String get_files_path(CommandLine cmd) {
    String available_files_path;
    if (cmd.hasOption("path")) {
      available_files_path = cmd.getOptionValue("path");
    } else {
      available_files_path = default_files_path;
    }
    return available_files_path;
  }

  static Daemon createDaemon(CommandLine cmd) {

    Daemon daemon = new Daemon(get_files_path(cmd));

    if (cmd.hasOption("dai")) {
      daemon.setDaemonAddress(cmd.getOptionValue("dai"));

    }
    if (cmd.hasOption("dap")) {
      try {
        int port = Integer.parseInt(cmd.getOptionValue("dap"));
        daemon.setDaemonPort(port);
      } catch (NumberFormatException e) {
        exit_with_error(e.toString());

      }
    }

    if (cmd.hasOption("dii")) {
      daemon.setDiaryAddress(cmd.getOptionValue("dii"));

    }
    if (cmd.hasOption("dip")) {
      try {
        int port = Integer.parseInt(cmd.getOptionValue("dip"));
        daemon.setDiaryPort(port);
      } catch (NumberFormatException e) {
        exit_with_error(e.toString());

      }
    }

    if (cmd.hasOption("s")) {
      daemon.makeSummary();
      System.exit(0);
    }
    return daemon;

  }

  public static void main(String[] args) {
    CommandLine cmd = null;
    try {
      cmd = handle_cli(args);
      // Print help message
      if (cmd.hasOption("help")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("daemon", create_options());
        return;
      }
    } catch (ParseException exp) {
      System.err.println("Parsing failed.  Reason: " + exp.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("daemon", create_options());
      System.exit(-1);
    }

    Daemon daemon = createDaemon(cmd);

    daemon.notify_diary();
    // try {
    // FileProvider stub = (FileProvider) UnicastRemoteObject.exportObject(daemon,
    // 0);
    // Registry registry;
    // try {
    // registry = LocateRegistry.createRegistry(port);
    // } catch (RemoteException e) {
    // registry = LocateRegistry.getRegistry(port);
    // }
    // registry.rebind("FileProvider", stub);

    // System.err.println("Server ready");
    // } catch (Exception e) {
    // System.err.println("Server exception: " + e.toString());
    // e.printStackTrace();
    // }
  }
}
