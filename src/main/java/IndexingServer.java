import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class of IndexingServer
 */
public class IndexingServer extends Server implements Runnable{
    // fileName -> address + port
    private Map<String, List<String>> fileRecorder;
    // For polling algorithm, fileName -> index
    private Map<String, Integer> pollingIndexer;
    // Keep a socket for indexing server
    private ServerSocket serverSocket;
    // Manage threads
    private ThreadPoolExecutor threadPoolExecutor;
    public IndexingServer(String name, String address, int port) throws IOException {
        this.name = name;
        this.address = address;
        this.port = port;
        this.fileRecorder = new HashMap<String, List<String>>();
        this.pollingIndexer = new HashMap<String, Integer>();
        this.serverSocket = new ServerSocket(port);
        this.threadPoolExecutor = new ThreadPoolExecutor(4, 8, 1000,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

        System.out.println("Activate " + name + " " + address + " " + String.valueOf(port));
        // Use another thread to run indexing server
        new Thread(this).start();
    }

    /*
    Check whether fileName exists in servers
     */
    synchronized private void search(String fileName, Socket socket){
        RequestPackage rp;
        System.out.println("Finding " + fileName);
        if (fileRecorder.containsKey(fileName)) {
            List<String> res = new ArrayList<String>();
            List<String> fileNames = fileRecorder.get(fileName);
            // Applying polling algorithm, put the recommended server in the first place
            int _index = pollingIndexer.get(fileName);
            int index = _index % fileNames.size();
            res.add(fileNames.get(index));
            for (int i = 0; i < fileNames.size(); i++){
                if (i != index)
                    res.add(fileNames.get(i));
            }
            rp = new RequestPackage(1, this.address, this.port, res);
            pollingIndexer.put(fileName, _index + 1);
        }else{
            rp = new RequestPackage(-1, this.address, this.port, null);
        }
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(rp);
            oos.flush();
            oos.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
    Since many requests will register their files at the same time, register method must synchronize here
     */
    synchronized private void register(String address, int port, List<String> fileNames){
        for (String fileName : fileNames){
            String fullAddress = address + ";" + String.valueOf(port);
            if (fileRecorder.containsKey(fileName)){
                fileRecorder.get(fileName).add(fullAddress);
            }else{
                List<String> addressList = new ArrayList<String>();
                addressList.add(fullAddress);
                fileRecorder.put(fileName, addressList);
                pollingIndexer.put(fileName, 0);
            }
            System.out.println("Register " + fileName + " Successfully!");
        }

    }

    /*
    Implement Runnable interface that this server can run in another thread
     */
    public void run() {
        Socket socket = null;
        try {
            // keep accepting socket
            while (true) {
                socket = this.serverSocket.accept();
                // use another thread to process this socket
                threadPoolExecutor.execute(new Task(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A class for processing socket
     */
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
                // different types mean different requests
                if (rp.getRequestType() == 0){
                    register(rp.getRequestAddress(), rp.getRequestPort(), rp.getFileNames());
                }else if (rp.getRequestType() == 1){
                    search(rp.getFileNames().get(0), this.socket);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
