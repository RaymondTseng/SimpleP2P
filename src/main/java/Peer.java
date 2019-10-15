import java.io.*;
import java.net.*;
import java.util.*;

public class Peer extends Server implements Runnable{
    private Map<String, String> localFiles;
    private String dataFolder;
    private ServerSocket serverSocket;
    public Peer(String name, String address, int port, String dataFolder) throws IOException{
        this.name = name;
        this.address = address;
        this.port = port;
        this.dataFolder = dataFolder;
        this.localFiles = new HashMap<String, String>();
        initializeLocalFiles();
        serverSocket = new ServerSocket(port);
        System.out.println("Activate " + name + " " + address + " " + String.valueOf(port));
    }


    public void registerFile(String fileName, String address, int port){
        try {
            Socket socket = new Socket(address, port);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            List<String> fileNames = new ArrayList<String>() ;
            fileNames.add(fileName);
            RequestPackage rp = new RequestPackage(0, this.address, this.port, fileNames);
            oos.writeObject(rp);
            oos.flush();
            oos.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void registerAllFiles(String address, int port){
        for (String fileName : localFiles.keySet()){
            registerFile(fileName, address, port);
        }
    }

    public void initializeLocalFiles(){
        File file = new File(this.dataFolder);
        File[] fileList = file.listFiles();
        if (fileList == null)
            return;
        for (int i = 0; i < fileList.length; i++){
            System.out.println("Register " + fileList[i].getName());
            localFiles.put(fileList[i].getName(), file.getAbsolutePath());
        }
    }

    private boolean containsFile(String fileName){
        return true;
    }

    public File obtain(String fileName){
        return null;
    }

    public void run() {
        //server的accept方法是阻塞式的
        Socket socket = null;
        try {
            socket = this.serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new Task(socket)).start();
    }


    class Task implements Runnable{
        private Socket socket;
        public Task(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            ObjectOutputStream oos = null;
            try{
                oos = new ObjectOutputStream(this.socket.getOutputStream());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
