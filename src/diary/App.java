
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
    public static int default_port = 8081;

    public static Options create_options() {
        Options options = new Options();

        Option help = new Option("h", "help", false, "Print this help message");
        Option portOpt = new Option("port", true, "Port to use");

        options.addOption(help);
        options.addOption(portOpt);
        return options;
    }

    public static CommandLine handle_cli(String[] args) throws ParseException {
        Options options = create_options();

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        return cmd;
    }

    public static int get_port(CommandLine cmd) {
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
            formatter.printHelp("diary", create_options());
            System.exit(-1);
        }

        // Print help message
        if (cmd.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("diary", create_options());
            return;
        }


        int port = get_port(cmd);

        Registry registry;
        // launching naming service
        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            try {
                registry = LocateRegistry.getRegistry(port);
            } catch (RemoteException e1) {
                System.err.println("Server error : can't get the register");
                e1.printStackTrace();
            }
        }

        DiaryImpl diary;
        try {
            // Create a instance of the server object
            diary = new DiaryImpl();
        } catch (Exception e) {
            diary = null;
        }
        try {


            String URL = "//" + InetAddress.getLocalHost().getHostAddress() + ":" + port + "/my_server_daemon";
            // Register the object with the naming service
            Naming.rebind(URL, diary);
            System.out.println("Diary bound in registry Daemon");
            
            URL = "//" + InetAddress.getLocalHost().getHostAddress() + ":" + port + "/my_server_downloader";
            // Register the object with the naming service
            Naming.rebind(URL, diary);
            System.out.println("Diary bound in registry Downloader");          
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
