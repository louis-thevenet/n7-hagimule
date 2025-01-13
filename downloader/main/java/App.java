package main.java;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** App launch a Downloader app. */
public class App {

  /** The default port number for the app */
  public static final int defaultPort = 8081;

  /**
   * Create options in the commande line.
   *
   * @return the options
   */
  public static Options createOptions() {

    Options options = new Options();
    Option help = new Option("h", "help", false, "Print this help message");
    Option ls = new Option("l", "list", false, "Request a list of available files");

    Option pathOpt = new Option("p", "path", true, "Path to store downloaded files");
    Option downloaderAddressOpt =
        new Option("doi", "downloader-ip", true, "Address to use to download files");
    Option diaryAddressOpt =
        new Option("dii", "diary-ip", true, "Address to use to register files to the diary");
    Option diaryPortOpt =
        new Option("dip", "diary-port", true, "Port to use to register files to the diary");

    options.addOption(help);
    options.addOption(ls);
    options.addOption(pathOpt);
    options.addOption(downloaderAddressOpt);
    options.addOption(diaryAddressOpt);
    options.addOption(diaryPortOpt);

    return options;
  }

  /**
   * Stop the program.
   *
   * @param error the string of the error.
   */
  static void exitWithError(String error) {
    System.err.println(error);
    System.exit(-1);
  }

  /**
   * Handler of the command line.
   *
   * @param args Arguments of the commande line
   * @return the parsed command line
   * @throws ParseException if the parser failed
   */
  public static CommandLine handleCli(String[] args) throws ParseException {
    Options options = createOptions();
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);
    return cmd;
  }

  /**
   * Get the port number registered in the command line or default value.
   *
   * @param cmd the parsed command line
   * @return the number the the port
   */
  public static int getDiaryPort(CommandLine cmd) {
    int port = defaultPort;
    if (cmd.hasOption("dip")) {
      try {
        port = Integer.parseInt(cmd.getOptionValue("dip"));
      } catch (Exception e) {
      }
    }
    return port;
  }

  /**
   * Get the ip server from the command line.
   *
   * @param cmd the command line
   * @return the ip of the server give in command line, else default.
   */
  public static String getDiaryIpServer(CommandLine cmd) {
    if (cmd.hasOption("dii")) {
      try {
        return cmd.getOptionValue("dii");
      } catch (Exception e) {
        exitWithError("Could not retrieve cli argument: " + e);
      }
      try {
        return "//" + InetAddress.getLocalHost().getHostAddress();
      } catch (UnknownHostException e2) {
        exitWithError("Could not retrieve local address: " + e2);
      }
    }
    exitWithError("No address found for dirary");
    return null;
  }

  /**
   * Main function.
   *
   * @param args args of command line
   */
  public static void main(String[] args) {
    CommandLine cmd = null;
    try {
      cmd = handleCli(args);
      if (cmd.hasOption("help")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("downloader", createOptions());
        return;
      }

    } catch (ParseException exp) {
      System.err.println("Parsing failed. Reason: " + exp.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("downloader", createOptions());
      System.exit(-1);
    }

    Downloader dl = new Downloader();

    if (cmd.hasOption("dii")) {
      dl.setDiaryAddress(cmd.getOptionValue("dii"));
    }
    if (cmd.hasOption("doi")) {
      dl.setDownloaderAddress(cmd.getOptionValue("doi"));
    }
    if (cmd.hasOption("dip")) {
      try {
        int port = Integer.parseInt(cmd.getOptionValue("dip"));
        dl.setDiaryPort(port);
      } catch (NumberFormatException e) {
        exitWithError(e.toString());
      }
    }

    if (cmd.hasOption("l")) {
      dl.listFiles();
    }

    if (cmd.hasOption("p")) {
      dl.setDownloadPath(cmd.getOptionValue("p"));
    }

    final String[] filesToDownload = cmd.getArgs();
    for (String f : filesToDownload) {
      dl.download(f);
    }
  }
}
