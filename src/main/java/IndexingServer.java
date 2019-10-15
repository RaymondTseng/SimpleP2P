import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;

public class IndexingServer extends Server implements Runnable{
    // fileName -> address + port
    private Map<String, Set<String>> fileRecorder;
    private ServerSocket serverSocket;
    public IndexingServer(String name, String address, int port) throws IOException {
        this.name = name;
        this.address = address;
        this.port = port;
        this.fileRecorder = new HashMap<String, Set<String>>();
        this.serverSocket = new ServerSocket(port);
        System.out.println("Activate " + name + " " + address + " " + String.valueOf(port));
    }

    private Set<String> search(String fileName){
        Set<String> res = new HashSet<String>();
        return res;
    }

    public void register(String address, int port, List<String> fileNames){
        for (String fileName : fileNames){
            String fullAddress = address + "/" + String.valueOf(port);
            if (fileRecorder.containsKey(fileName)){
                fileRecorder.get(fileName).add(fullAddress);
            }else{
                Set<String> addressSet = new HashSet<String>();
                addressSet.add(fullAddress);
                fileRecorder.put(fileName, addressSet);
            }
        }

    }

    public void run() {
        //server的accept方法是阻塞式的
        Socket socket = null;
        try {
            while (true) {
                socket = this.serverSocket.accept();
                new Thread(new Task(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class Task implements Runnable{
        private Socket socket;
        public Task(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            ObjectInputStream ois = null;
            try{
                ois = new ObjectInputStream(this.socket.getInputStream());
                RequestPackage rp = (RequestPackage) ois.readObject();
                if (rp.getRequestType() == 0){
                    register(rp.getRequestAddress(), rp.getRequestPort(), rp.getFileNames());
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
