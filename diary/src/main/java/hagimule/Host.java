package hagimule;

import java.io.Serializable;

public class Host implements Serializable {
    
    private String ip;

    public Host(String ip) {
        this.ip = ip;
    }
}
