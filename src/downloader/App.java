
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.net.InetAddress;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class App {

    /**default port number of diary. */
    public static int defaultPort = 8081;

    private String filename;

    private int sizeOfFile;

    /**default ip address of the server (localhost). */
    public static String defaultIpServer = 
        "//" + InetAddress.getLocalHost().getHostAddress()
        + ":" + defaultPort + "/my_server_downloader";


    /**Create options in the commande line.
     * @return the options
    */
    public static Options createOptions() {
        Options options = new Options();

        Option help = new Option("h", "help", false, "Print this help message");
        Option ipServ = new Option("s", "server", true, "IP of the server to connect");
        Option portOpt = new Option("port", true, "Port to connect");

        options.addOption(help);
        options.addOption(portOpt);
        options.addOption(ipServ);
        return options;
    }

    /**Handler of the command line.
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

    /**Get the port number registered in the command line or default value.
     * @param cmd the parsed command line
     * @return the number the the port
    */
    public static int getPort(CommandLine cmd) {
        int port = defaultPort;
        if (cmd.hasOption("port")) {
            try {
                port = Integer.parseInt(cmd.getOptionValue("port"));
            } catch (Exception e) {
            }
        }
        return port;
    }

    /**Get the ip server from the command line.
     * @param cmd the command line
     * @return the ip of the server give in command line, else default.
     */
    public static int getIpServer(CommandLine cmd) {
        String ipServer = defaultIpServer;
        if (cmd.hasOption("server")) {
            try {
                ipServer = cmd.getOptionValue("server");
            } catch (Exception e) {
            }
        }
        return ipServer;
    }



    public static String computeDownload(List<Host> lh) {
        // size of Download done by each source
        int taskSize = Math.max(1, sizeOfFile / lh.length);
        // Set of thread
        Set<Thread> threads = new HashSet<>();
        // List of results
        HashMap<Integer, String> results = new LinkedList<>();
        // Lists of activities
        List<Downloader> jobs = new LinkedList<>();

        // Jobs creation
        int i = 0;
        for (Host h : lh) {
            jobs.add(new Downloader(h, filename, taskSize, i, results));
            i++;
        }

        // Jobs start
        for (Downloader d : jobs) {
            threads.add(new Thread(d));
            d.start();
        }

        // wait for end
        for (Thread t : threads) {
            t.join();
        }

        // results combination
        String res = "";
        for (int i = 0; i < lh.length; i++) {
            res.concat(results.get(i));
        }

        return res;

    }







    /**Main function.
     * @param args args of command line
     */
    public static void main(String[] args) {
        CommandLine cmd = null;
        try {
            cmd = handleCli(args);

            // Print help message
            if (cmd.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("downloader", create_options());
                return;
            }

            // get args : default if not define in option
            int port = getPort(cmd);
            String ipServer = getIpServer(cmd);
            
            if (cmd.getArgs().length == 0) {
                throw new RuntimeException("error: No file indicated");
            } else {
                this.filename = cmd.getArgs()[0];
            }



            // connect to the diary
            DiaryDownloader diairy = (DiaryDownloader) Naming.lookup(ipServer);

            // search of Hosts of the file
            List<Host> = new ArrayList<>();
            this.sizeOfFile = diairy.whichHosts(filename, lh);

            // Some hosts has been found, else FileIsNotAvailableException is raised
            String res = computeDownload(lh, sizeOfFile);

        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("downloader", create_options());
            System.exit(-1);
        } catch (RemoteException e) {
            System.err.println("Remote Exception raised");
            e.printStackTrace();
        } catch (FileIsNotAvailableException e) {
            System.err.println(e.getMessage());
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}