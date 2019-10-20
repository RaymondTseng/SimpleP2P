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
        new Thread(this).start();
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

    public void createFile(String fileName){
        File temp = new File(this.dataFolder);
        File[] files = temp.listFiles();
        if (files != null){
            for (File f : files){
                if (f.getName().equals(fileName)){
                    System.out.println("File already exists!");
                    return;
                }
            }
        }
        File file = new File(this.dataFolder + "/" + fileName);

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

    public String searchFile(String fileName, String address, int port){
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
            oos.close();
            ois.close();
            if (rp.getRequestType() != -1) {
                // simple strategy
                return rp.getFileNames().get(0);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("File does not exist!");
        return "";
    }

    public void obtainFile(String fileName, String address, int port){
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }

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
            while (true){
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
                if (rp.getRequestType() == 2){
                    sendFile(rp.getFileNames().get(0), this.socket);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
