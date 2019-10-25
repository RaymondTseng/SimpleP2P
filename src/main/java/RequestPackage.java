import java.io.Serializable;
import java.util.List;

/**
 * Class for building a request
 */
public class RequestPackage implements Serializable {
    private static final long serialVersionUID = 1386583756403881124L;
    // -1 -> failure, 0 -> register, 1 -> search, 2 -> obtain
    private int requestType;
    private String requestAddress;
    private int requestPort;
    private List<String> fileNames;

    public RequestPackage(int requestType, String address, int port, List<String> fileNames) {
        this.requestType = requestType;
        this.requestAddress = address;
        this.requestPort = port;
        this.fileNames = fileNames;
    }

    public int getRequestType() {
        return requestType;
    }

    public String getRequestAddress() {
        return requestAddress;
    }

    public int getRequestPort() {
        return requestPort;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

}
