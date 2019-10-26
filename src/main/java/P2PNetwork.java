import com.alibaba.fastjson.JSONObject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
        readConfigFile(configFilePath);
        // register the each peers' files
        registerAllPeersFiles();
    }

    public P2PNetwork(String configFilePath, int M){
        peerList = new ArrayList<Peer>();
        // read the config file for this peer2peer network
        readConfigFile(configFilePath);
        // create M files with random size, assign these files to peers randomly
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
                network.testMode();
            } else {
                System.out.println("Manual mode");
                network.manualMode();
            }
        }catch (Exception e){
            e.printStackTrace();
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
    private void readConfigFile(String configFilePath){
        try {
            File file = new File(configFilePath);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String strLine = null;
            while(null != (strLine = bufferedReader.readLine())){
                String[] configArray = strLine.split(" ");
                System.out.println("-------------------------------------------------");
                if (configArray[0].equals("indexingServer")){
                    // constructing indexing server
                    this.indexingServer = new IndexingServer(configArray[0], configArray[1], Integer.parseInt(configArray[2]));
                }else{
                    // constructing each peers
                    peerList.add(new Peer(configArray[0], configArray[1], Integer.parseInt(configArray[2]), configArray[3]));
                }
                System.out.println("-------------------------------------------------");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
