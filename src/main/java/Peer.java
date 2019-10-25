import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A class for peer
 */
public class Peer extends Server implements Runnable{
    // fileName -> absolute path
    private Map<String, String> localFiles;
    // current peer's folder
    private String dataFolder;
    // keep a socket that the peer can accept socket constantly
    private ServerSocket serverSocket;
    /*
    Construct method
     */
    public Peer(String name, String address, int port, String dataFolder) throws IOException{
        this.name = name;
        this.address = address;
        this.port = port;
        this.dataFolder = dataFolder;
        this.localFiles = new HashMap<String, String>();
        initializeLocalFiles();
        serverSocket = new ServerSocket(port);
        System.out.println("Activate " + name + " " + address + " " + String.valueOf(port));
        // use another thread to run this peer
        new Thread(this).start();
    }


    /**
     * Send a request to indexing server to register a file
     * @param fileName
     * @param address the address of indexing server
     * @param port the port of indexing server
     */
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

    /**
     * Create a file
     * @param fileName
     * @return
     */
    public boolean createFile(String fileName){
        File temp = new File(this.dataFolder);
        File[] files = temp.listFiles();
        if (files != null){
            for (File f : files){
                if (f.getName().equals(fileName)){
                    System.out.println("File already exists!");
                    return false;
                }
            }
        }
        try {
            File file = new File(this.dataFolder + "/" + fileName);
            boolean res = file.createNewFile();
            return res;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * register all local files in this peer
     * @param address the address of indexing server
     * @param port the port of indexing server
     */
    public void registerAllFiles(String address, int port){
        for (String fileName : localFiles.keySet()){
            registerFile(fileName, address, port);
        }
    }

    /**
     * Check the folder and put all file names in hash map
     */
    public void initializeLocalFiles(){
        File file = new File(this.dataFolder);
        File[] fileList = file.listFiles();
        if (fileList == null)
            return;
        for (int i = 0; i < fileList.length; i++){
            System.out.println("Register " + fileList[i].getName());
            localFiles.put(fileList[i].getName().trim(), file.getAbsolutePath());
        }
    }

    /**
     * Put the file name in hash map and register this file
     * @param fileName
     * @param address the address of indexing server
     * @param port the port of indexing server
     */
    public void initialNewFile(String fileName, String address, int port){
        File file = new File(this.dataFolder + "/" + fileName);
        localFiles.put(fileName, file.getAbsolutePath());
        registerFile(fileName, address, port);

    }

    /**
     * Send a request to indexing server, get the response from indexing server
     * @param fileName
     * @param address the address of indexing server
     * @param port the port of indexing server
     * @return all peers which hold fileName
     */
    public List<String> searchFile(String fileName, String address, int port){
        try{
            Socket socket = new Socket(address, port);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            List<String> fileNames = new ArrayList<String>();
            fileNames.add(fileName);
            RequestPackage rp = new RequestPackage(1, this.address, this.port, fileNames);

            oos.writeObject(rp);
            oos.flush();


            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            rp = (RequestPackage) ois.readObject();
            ois.close();
            oos.close();
            if (rp.getRequestType() != -1){
                System.out.println("File exists in");
                for (String name : rp.getFileNames()){
                    System.out.println(name);
                }
                return rp.getFileNames();
            }


        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("File does not exist!");
        return null;
    }

    /**
     * obtain file from another peer
     * @param fileName
     * @param address the address of peer
     * @param port the port of peer
     */
    public void obtainFile(String fileName, String address, int port){
        if (localFiles.containsKey(fileName)){
            System.out.println(fileName + " already exists in this peer!");
            return;
        }
        try{
            Socket socket = new Socket(address, port);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            List<String> fileNames = new ArrayList<String>();
            fileNames.add(fileName);
            RequestPackage rp = new RequestPackage(2, this.address, this.port, fileNames);
            oos.writeObject(rp);
            oos.flush();

            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(this.dataFolder + "/" + fileName));

            byte [] buf = new byte [1024];
            int len = 0;
            while((len = bis.read(buf))!=-1){
                bos.write(buf, 0, len);
            }
            bos.flush();
            oos.close();
            bos.close();
            bis.close();
            System.out.println("Download " + fileName + " successfully!");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Send a file to another peer
     * @param fileName
     * @param socket
     */
    public void sendFile(String fileName, Socket socket){
        try {

            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(this.dataFolder + "/" + fileName));

            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = bis.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }

        bos.close();
        bis.close();
    }catch (Exception e){
        e.printStackTrace();
    }
}

    public void run() {
        Socket socket = null;
        try {
            // accept sockets constantly
            while (true){
                socket = this.serverSocket.accept();
                // use another thread to process this socket
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
                if (rp.getRequestType() == 2){
                    sendFile(rp.getFileNames().get(0), this.socket);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
