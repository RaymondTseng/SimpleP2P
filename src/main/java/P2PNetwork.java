
import java.io.*;
import java.util.*;


/**
 * Main class for running, including an indexing server and a list of peers.
 */
public class P2PNetwork {
    private IndexingServer indexingServer;
    private List<Peer> peerList;
    /*
    Constructor method
     */
    public P2PNetwork(String configFilePath){
        peerList = new ArrayList<Peer>();
        // read the config file for this peer2peer network
        List<String[]> networkInformation = readConfigFile(configFilePath);
        // initialize network
        initializeNetwork(20, networkInformation);
        // register the each peers' files
        registerAllPeersFiles();
    }

    public P2PNetwork(String configFilePath, int M, int N, int f){
        peerList = new ArrayList<Peer>();
        // read the config file for this peer2peer network
        List<String[]> networkInformation = readConfigFile(configFilePath);
        // initialize network
        initializeNetwork(M, networkInformation);
        // register the each peers' files
        registerAllPeersFiles();
    }
    /*
    Main Method
     */
    public static void main(String[] args) {
        String configFilePath = "./config.txt";
        try {
            P2PNetwork network = new P2PNetwork(configFilePath);
            if (args.length == 3) {
                System.out.println("Test mode");
                // args [M, N, f]
                // M -> files, N -> requests, f -> frequency
                // M must larger than 5 since there are 5 peers in this network
                int M = Integer.parseInt(args[0]);
                int N = Integer.parseInt(args[1]);
                int f = Integer.parseInt(args[2]);
                network.testMode();
            } else {
                System.out.println("Manual mode");
                network.manualMode();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void initializeNetwork(int M, List<String[]> networkInformation){
        if (networkInformation.equals(null)){
            System.out.println("fail to initialize network");
            System.exit(0);
        }
        List<String> dataFolders = new ArrayList<String>();
        for (int i = 0; i < networkInformation.size(); i++){
            if (i != 0){
                dataFolders.add(networkInformation.get(i)[3]);
            }
        }
        try {
            initializeFileWithSize(M, dataFolders);
            for (int i = 0; i < networkInformation.size(); i++) {
                String[] configArray = networkInformation.get(i);
                if (i == 0) {
                    this.indexingServer = new IndexingServer(configArray[0], configArray[1], Integer.parseInt(configArray[2]));
                } else {
                    peerList.add(new Peer(configArray[0], configArray[1], Integer.parseInt(configArray[2]), configArray[3]));
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initializeRequestsQueue(int M, int N){
        List<String> requests = new ArrayList<String>();
        List<Set<String>> peerFilesRecorder = new ArrayList<Set<String>>();

        for (int i = 0; i < peerList.size(); i++){
            peerFilesRecorder.add(new HashSet<String>(peerList.get(i).getLocalFiles().keySet()));
        }
        Random r = new Random();
        for (int i = 0; i < N; i++){
            int index = r.nextInt(peerList.size());
            while (peerFilesRecorder.get(index).size() == M){
                index = r.nextInt(peerList.size());
            }
            String res = null;
            for (int j = 0; j < M; j++){
                String temp = String.valueOf(j) + ".txt";
                if (!peerFilesRecorder.get(index).contains(temp)){
                    res = temp;
                }
            }
            if (res != null){

            }

        }
    }

    private void initializeFileWithSize(int M, List<String> dataFolders) throws IOException{
        Random r = new Random();
        for (int i = 0; i < M; i++){
            String fileName = String.valueOf(i) + ".txt";
            int fileSize = (int) Math.floor(r.nextDouble() * 90000) + 10000;
            fileSize = fileSize < 0 ? - fileSize : fileSize;
            File file = null;
            for (int j = 0; j < dataFolders.size(); j++){
                if (r.nextFloat() <= 0.4){
                    if (file == null){
                        file = new File(dataFolders.get(j) + "/" + fileName);
                        RandomAccessFile raf = new RandomAccessFile(file, "rw");
                        raf.setLength(fileSize);
                    }else{
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dataFolders.get(j) + "/" + fileName));

                        byte [] buf = new byte [1024];
                        int len = 0;
                        while((len = bis.read(buf))!=-1){
                            bos.write(buf, 0, len);
                        }
                        bos.flush();
                        bos.close();
                        bis.close();
                    }
                }
            }
        }



    }

    public void testMode() throws Exception{

    }

    public void manualMode() throws Exception{
        // wait for registering
        Thread.sleep(2000);
        System.out.println("Set up a peer by entering the PEER ID (a, b, c, d, e) :)");
        Scanner userInput = new Scanner(System.in);
        String varInput = userInput.nextLine();
        Peer p = findPeerByName(varInput);
        if (p == null){
            System.out.println("Peer Id doesn't exist");
            System.exit(0);
        }
        while (true) {
            System.out.println("*******************************************************");
            System.out.println("Enter 1 : Create and register a file.");
            System.out.println("Enter 2 : Search a file on peers.");
            System.out.println("Enter 3 : Download file from a peer.");
            System.out.println("Enter 4 : To exit the program.");
            System.out.println("*******************************************************");

            varInput = userInput.nextLine();

            if ("1".equals(varInput)) {
                System.out.println("Enter the file name: ");
                varInput = userInput.nextLine();
                boolean ifSuccess = p.createFile(varInput, 10000);
                if (ifSuccess) {
                    System.out.println("Create " + varInput + " Successfully!");
                    p.initialLocalFile(varInput, indexingServer.getAddress(), indexingServer.getPort());
                }else{
                    System.out.println("Create " + varInput + " Unsuccessfully!");
                }
            }else if ("2".equals(varInput)){
                System.out.println("Enter the file name: ");
                varInput = userInput.nextLine();
                List<String> addressPortList = p.searchFile(varInput, indexingServer.getAddress(),
                        indexingServer.getPort());
            }else if ("3".equals(varInput)){
                System.out.println("Enter the file name: ");
                varInput = userInput.nextLine();
                List<String> addressPortList = p.searchFile(varInput, indexingServer.getAddress(),
                        indexingServer.getPort());
                String[] array = addressPortList.get(0).split(";");
                if (array.length != 2)
                    break;
                p.obtainFile(varInput, array[0], Integer.parseInt(array[1]));
                p.initialLocalFile(varInput, indexingServer.getAddress(), indexingServer.getPort());
            }else if ("4".equals(varInput)) {
                System.exit(0);
            }else{
                System.out.println("Wrong command, Please try again!!");
            }

        }
    }

    public Peer findPeerByName(String name){
        for (Peer p : this.peerList){
            if (p.name.equals(name)){
                return p;
            }
        }
        return null;
    }

    /*
    register all peers' files
     */
    private void registerAllPeersFiles(){
        for (Peer p : peerList){
            p.registerAllFiles(this.indexingServer.getAddress(), this.indexingServer.getPort());
        }
    }
    /*
    read config file for constructing peer2peer network
     */
    private List<String[]> readConfigFile(String configFilePath){
        List<String[]> networkInformation = new ArrayList<String []>();
        BufferedReader bufferedReader;
        try {
            File file = new File(configFilePath);
            bufferedReader = new BufferedReader(new FileReader(file));
            String strLine = null;
            while(null != (strLine = bufferedReader.readLine())){
                String[] configArray = strLine.split(" ");
                if (configArray[0].equals("indexingServer")){
                    networkInformation.add(new String[] {configArray[0], configArray[1], configArray[2], ""});
                }else{
                    networkInformation.add(configArray);
                }
            }
            bufferedReader.close();
            return networkInformation;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
