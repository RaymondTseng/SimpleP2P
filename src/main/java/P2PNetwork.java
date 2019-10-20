import com.alibaba.fastjson.JSONObject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class P2PNetwork {
    private IndexingServer indexingServer;
    private List<Peer> peerList;
    public P2PNetwork(String configFilePath){
        peerList = new ArrayList<Peer>();
        readConfigFile(configFilePath);
        registerAllPeersFiles();
    }
    public static void main(String[] args) throws Exception{
        String configFilePath = "./config.txt";
        P2PNetwork network = new P2PNetwork(configFilePath);

        System.out.println("Set up a peer by entering the PEER ID (a, b, c, d, e) :)");
        Scanner userInput = new Scanner(System.in);
        String varInput = userInput.nextLine();
        Peer p = network.findPeerByName(varInput);
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
                p.createFile(varInput);
                p.registerFile(varInput, network.indexingServer.getAddress(), network.indexingServer.getPort());
            }else if ("2".equals(varInput)){
                System.out.println("Enter the file name: ");
                varInput = userInput.nextLine();
                String addressPort = p.searchFile(varInput, network.indexingServer.getAddress(), network.indexingServer.getPort());
                System.out.println(addressPort);
            }else if ("3".equals(varInput)){
                System.out.println("Enter the file name: ");
                varInput = userInput.nextLine();
                String addressPort = p.searchFile(varInput, network.indexingServer.getAddress(), network.indexingServer.getPort());
                String[] array = addressPort.split(";");
                if (array.length != 2)
                    break;
                p.obtainFile(varInput, array[0], Integer.parseInt(array[1]));
                System.out.println("Download " + varInput + " successfully!");
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
    private void registerAllPeersFiles(){
        for (Peer p : peerList){
            p.registerAllFiles(this.indexingServer.getAddress(), this.indexingServer.getPort());
        }
    }
    private void readConfigFile(String configFilePath){
        try {
            File file = new File(configFilePath);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String strLine = null;
            while(null != (strLine = bufferedReader.readLine())){
                String[] configArray = strLine.split(" ");
                System.out.println("-------------------------------------------------");
                if (configArray[0].equals("indexingServer")){
                    this.indexingServer = new IndexingServer(configArray[0], configArray[1], Integer.parseInt(configArray[2]));
                }else{
                    peerList.add(new Peer(configArray[0], configArray[1], Integer.parseInt(configArray[2]), configArray[3]));
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
