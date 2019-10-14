import java.io.*;
import java.util.*;

public class Peer extends Server{
    private Map<String, String> registeredFiles;
    private String dataFolder;
    public Peer(String address, int port, String dataFolder){
        this.address = address;
        this.port = port;
        this.dataFolder = dataFolder;
    }
    public void registerFile(String fileName){

    }

    public void registerAllFiles(){

    }

    private boolean containsFile(String fileName){
        return true;
    }

    public File obtain(String fileName){
        return null;
    }

}
