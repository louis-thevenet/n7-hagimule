
import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class Downloader extends UnicastRemoteObject {

    public static void main(String args[]) {
        if (args.length <= 2) {
            System.err.println("Args are not valids");
        }
        String filename = args[1];

        try {
            // get the stub of the server object from the rmiregistry
            DiaryDownloader diary = (DiaryDownloader) Naming.lookup("//localhost:8082/my_server");

            // request file hosts to the diary
            List<Host> lh = diary.whichHosts(args[0]);

            // asks the fragment of the file to each hosts
            for (Host h : lh) {
                // get the stub of the server object from the rmiregistry
                Deamon deamon = (DiaryDownloader) Naming.lookup(h.getIp());

                String file = deamon.Download();
            }

        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
       
    
}