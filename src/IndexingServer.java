import java.util.*;

public class IndexingServer extends Server{
    // fileName -> address + port
    private Map<String, Set<String>> fileRecorder;
    public IndexingServer(String address, int port){
        this.address = address;
        this.port = port;
        this.fileRecorder = new HashMap<>();
    }

    private Set<String> search(String fileName){
        Set<String> res = new HashSet<>();
        return res;
    }

    public void register(String address, int port, List<String> fileNames){
        for (String fileName : fileNames){
            String fullAddress = address + "/" + String.valueOf(port);
            if (fileRecorder.containsKey(fileName)){
                fileRecorder.get(fileName).add(fullAddress);
            }else{
                Set<String> addressSet = new HashSet<>();
                addressSet.add(fullAddress);
                fileRecorder.put(fileName, addressSet);
            }
        }

    }

}
