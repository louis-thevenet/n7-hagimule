package main.java;

import java.rmi.RemoteException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class App {
  static String homeDir = System.getProperty("user.home");
  static String defaultFilesPath = homeDir + "/Downloads/";

  static Options createOptions() {
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

  static void exitWithError(String error) {
    System.err.println(error);
    System.exit(-1);

  }

  static CommandLine handleCLI(String[] args) throws ParseException {
    Options options = createOptions();

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);
    return cmd;
  }

  static String getFilesPath(CommandLine cmd) {
    String availableFilesPath;
    if (cmd.hasOption("path")) {
      availableFilesPath = cmd.getOptionValue("path");
    } else {
      availableFilesPath = defaultFilesPath;
    }
    return availableFilesPath;
  }

  static Daemon createDaemon(CommandLine cmd) {

    Daemon daemon = null;
    try {
      daemon = new Daemon(getFilesPath(cmd));
    } catch (RemoteException e) {
      exitWithError("Failed to create daemon object: " + e);
    }

    if (cmd.hasOption("dai")) {
      daemon.setDaemonAddress(cmd.getOptionValue("dai"));

    }
    if (cmd.hasOption("dap")) {
      try {
        int port = Integer.parseInt(cmd.getOptionValue("dap"));
        daemon.setDaemonPort(port);
      } catch (NumberFormatException e) {
        exitWithError(e.toString());

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
        exitWithError(e.toString());

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

    daemon.notifyDiary();
    daemon.listen();
    daemon.startNotifying();
  }
}
