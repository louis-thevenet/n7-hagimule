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
  static final int default_port = 8081;

  static Options create_options() {
    Options options = new Options();

    Option help = new Option("h", "help", false, "Print this help message");
    Option pathOpt = new Option("p", "path", true, "Path to files to make available");
    Option portOpt = new Option("port", true, "Port to use");

    options.addOption(help);
    options.addOption(pathOpt);
    options.addOption(portOpt);
    return options;
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

  static int get_port(CommandLine cmd) {
    int port = default_port;
    if (cmd.hasOption("port")) {
      try {
        port = Integer.parseInt(cmd.getOptionValue("port"));
      } catch (Exception e) {

      }
    }
    return port;
  }

  public static void main(String[] args) {
    CommandLine cmd = null;
    try {
      cmd = handle_cli(args);
    } catch (ParseException exp) {
      System.err.println("Parsing failed.  Reason: " + exp.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("daemon", create_options());
      System.exit(-1);
    }

    // Print help message
    if (cmd.hasOption("help")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("daemon", create_options());
      return;
    }

    Daemon daemon = new Daemon(get_files_path(cmd));
    int port = get_port(cmd);

    daemon.notify_diary();
    try {
      FileProvider stub = (FileProvider) UnicastRemoteObject.exportObject(daemon, 0);
      Registry registry;
      try {
        registry = LocateRegistry.createRegistry(port);
      } catch (RemoteException e) {
        registry = LocateRegistry.getRegistry(port);
      }
      registry.rebind("FileProvider", stub);

      System.err.println("Server ready");
    } catch (Exception e) {
      System.err.println("Server exception: " + e.toString());
      e.printStackTrace();
    }
  }
}
