package main.java;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;

public class App {
  public static int defaultPort = 8081;

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
    Option diaryAddressOpt = new Option("dii", "diary-ip", true, "Address to use to register files to the diary");
    Option diaryPortOpt = new Option("dip", "diary-port", true, "Port to use to register files to the diary");

    options.addOption(help);
    options.addOption(ls);
    options.addOption(pathOpt);
    options.addOption(diaryAddressOpt);
    options.addOption(diaryPortOpt);

    return options;
  }

  static void exit_with_error(String error) {
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
        exit_with_error("Could not retrieve cli argument: " + e);
      }
      try {
        return "//" + InetAddress.getLocalHost().getHostAddress();
      } catch (UnknownHostException e2) {
        exit_with_error("Could not retrieve local address: " + e2);
      }
    }
    exit_with_error("No address found for dirary");
    return null;
  }

  /*
   * public static String computeDownload(List<Host> lh) {
   * // size of Download done by each source
   * int taskSize = Math.max(1, sizeOfFile / lh.length);
   * // Set of thread
   * Set<Thread> threads = new HashSet<>();
   * // List of results
   * HashMap<Integer, String> results = new LinkedList<>();
   * // Lists of activities
   * List<Downloader> jobs = new LinkedList<>();
   * // Jobs creation
   * int i = 0;
   * for (Host h : lh) {
   * jobs.add(new Downloader(h, filename, taskSize, i, results));
   * i++;
   * }
   * // Jobs start
   * for (Downloader d : jobs) {
   * threads.add(new Thread(d));
   * d.start();
   * }
   * // wait for end
   * for (Thread t : threads) {
   * t.join();
   * }
   * // results combination
   * String res = "";
   * for (int i = 0; i < lh.length; i++) {
   * res.concat(results.get(i));
   * }
   * return res;
   * }
   */

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
    if (cmd.hasOption("dip")) {
      try {
        int port = Integer.parseInt(cmd.getOptionValue("dip"));
        dl.setDiaryPort(port);
      } catch (NumberFormatException e) {
        exit_with_error(e.toString());

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
