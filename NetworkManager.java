import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Create nodes, connect peers and provides broadcast api
public class NetworkManager {
    private final List<Node> nodes = new ArrayList<>();

    public Node createNode(String nodeId, int port){
        Node node = new Node(nodeId, port);
        nodes.add(node);
        return node;
    }

    public void connectPeers(Node a, Node b, String host ){
        a.addPeer(host, b.port);
        b.addPeer(host,a.port);
        System.out.println("Connected "+a.getNodeId()+" and "+b.getNodeId());
    }

    public void seedAll(Block genesis, HashMap<String, TransactionOutput> genesisUTXOs){
        for(Node node: nodes){
            node.seedGenesis(genesis,genesisUTXOs);
        }
    }

    public void startAll() {
        for (Node node : nodes) {
            node.start();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
            System.out.println("All nodes listening");
        }
    }
        public void stopAll(){
            for(Node node: nodes) node.stop();
            System.out.println("All nodes stopped");
    }

    public void printNetworkStatus(){
        System.out.println("Network Status:");
        for(Node node: nodes){
           node.printStatus();
        }

        boolean isSync = nodes.stream()
                .map(n -> n.getLocalChainTip() == null ? "null": n.getLocalChainTip().hash)
                .distinct()
                .count() == 1;
        System.out.println("All nodes in sync: " + isSync);
    }
    public List<Node> getNodes(){
        return nodes;
    }
}
