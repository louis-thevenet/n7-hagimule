package hagimule;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class DiaryImpl extends UnicastRemoteObject implements Diary {

    /**Diary implementation. */
    private HashMap<String, List<Host>> impl = new HashMap<>();
    
    public DiaryImpl() throws RemoteException {};

    @Override
    public List<Host> whichHosts(String file) throws RemoteException {
        List<Host> ret = impl.get(file);
        if (ret == null) {
            throw new RuntimeException("file isn't available");
        }
        return ret;
    }
    
    /** Diary is a server so he is running infinitely and produce Thread 
     * at each demmand.*/
    public static void main(String[] args) {
        try {
            // Create a instance of the server object
            Diary obj = new DiaryImpl();
            // Register the object with the naming service
            Naming.rebind("//my_machine/my_server", obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}