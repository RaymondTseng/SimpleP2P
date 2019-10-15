import com.alibaba.fastjson.JSONObject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class P2PNetwork {
    private IndexingServer indexingServer;
    private List<Peer> peerList;
    public P2PNetwork(String configFilePath){
        peerList = new ArrayList<Peer>();
        readConfigFile(configFilePath);
    }
    public static void main(String[] args) throws Exception{
        String configFilePath = "./config.txt";
        P2PNetwork network = new P2PNetwork(configFilePath);
        network.peerList.get(0).registerAllFiles(network.indexingServer.address, network.indexingServer.port);
        Thread.sleep(2000);
        System.out.println("done");
    }
    private void readConfigFile(String configFilePath){
        try {
            File file = new File(configFilePath);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String strLine = null;
            while(null != (strLine = bufferedReader.readLine())){
                String[] configArray = strLine.split(" ");
                if (configArray[0].equals("indexingServer")){
                    this.indexingServer = new IndexingServer(configArray[0], configArray[1], Integer.parseInt(configArray[2]));
                    new Thread(this.indexingServer).start();
                }else{
                    peerList.add(new Peer(configArray[0], configArray[1], Integer.parseInt(configArray[2]), configArray[3]));
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
