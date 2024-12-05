package hagimule;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class DiaryImpl extends UnicastRemoteObject implements DiaryDownLoader, DiaryDeamon {

    /**Diary implementation. */
    private HashMap<String, List<Host>> impl = new HashMap<>();
    
    public DiaryImpl() throws RemoteException {};

    @Override
    public List<Host> whichHosts(String file) 
            throws RemoteException, FileIsNotAvailableException {
        List<Host> ret = impl.get(file);
        if (ret == null) {
            throw new FileIsNotAvailableException("file isn't available");
        }
        return ret;
    }

    @Override
    public void registerFile(String ip, String file)
        throws RemoteException {
        Host h = new Host(ip);
        h.addFile(file);
        List<Host> l = impl.get(file);
        if (l == null) {
            l = new LinkedList<>();
            l.add(h);
            impl.put(file, l);
        } else {
            l.add(h);
        }
    }
    
    /** Diary is a server so he is running infinitely and produce Thread 
     * at each demmand.*/
    public static void main(String[] args) {
        int portDeamon = 8081;
        int portDownloeader = 8082;
        String URL;
        try {
            portDeamon = Integer.parseInt(args[0]);
            portDownloeader = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.err.println("No args");
            e.printStackTrace();
        }
        try {
            // launching naming service
            Registry registry1 = LocateRegistry.createRegistry(portDeamon);
            Registry registry2 = LocateRegistry.createRegistry(portDownloeader);


            // Create a instance of the server object
            DiaryImpl obj = new DiaryImpl();

            URL = "//" + InetAddress.getLocalHost().getHostAddress() + ":" + portDeamon + "/my_server";
            // Register the object with the naming service
            Naming.rebind(URL, obj);
            System.out.println("Diary bound in registry Deamon");
            
            URL = "//" + InetAddress.getLocalHost().getHostAddress() + ":" + portDownloeader + "/my_server";
            // Register the object with the naming service
            Naming.rebind(URL, obj);
            System.out.println("Diary bound in registry Downloader");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}