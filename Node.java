import java.io.*;
import java.net.*;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Node {
    public final int port;
    private final List<String> peerAddresses;
    private final String nodeId;

    private ServerSocket serverSocket;
    private boolean running = false;

    public ArrayList<Block> localBlockchain = new ArrayList<>();
    public HashMap<String, TransactionOutput> localUTXOs = new HashMap<>();

    public Node(String nodeId, int port){
        this.nodeId = nodeId;
        this.port = port;
        this.peerAddresses = new ArrayList<>();
    }

    // Start the node: begin listening for incoming connections and connect to peers
    public void addPeer(String host, int peerPort){
        peerAddresses.add(host +"-" + peerPort);
        System.out.println("Node " + nodeId + " added peer: " + host + ":" + peerPort);
    }

    // Both node must start from same genesis or they will never agree
    public void seedGenesis(Block genesis, HashMap<String, TransactionOutput> genesisUTXOs){
        localBlockchain.add(genesis);
        localUTXOs.putAll(genesisUTXOs);
        System.out.println("Node " + nodeId + " seeded with genesis block.");
    }

    public void start(){
        running = true;
        new Thread(this::listen, nodeId + "listener").start();
        System.out.println("Node " + nodeId + " started on port " + port);
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}

    }
    //
        private void listen(){
            try{
                serverSocket = new ServerSocket(port);
                while(running){
                    try{
                        Socket connection = serverSocket.accept();

                        // Handle each connection on its own thread so we can process multiple blocks at the same time
                        new Thread(() -> handleIncoming(connection), nodeId + "handler").start();
                    } catch(SocketException e ){
                        if(!running) break;
                    }
                }
            } catch(IOException e){
                System.out.println("Node " + nodeId + " failed to start: " + e.getMessage());
        }
    }

    // Called when peer send us a block
    private void handleIncoming(Socket connection){
        try (ObjectInputStream in = new ObjectInputStream (connection.getInputStream())){

            // Deserialize the Block object sent by the peer
            Block receivedBlock = (Block) in.readObject();
            System.out.println("Node " + nodeId + " received block: " + receivedBlock.hash.substring(0,12));
            receivedBlock(receivedBlock);
            } catch (Exception e){
            System.out.println("Node " + nodeId + " failed to handle incoming block: " + e.getMessage());
        }
    }

    // Only one thread at a time can modify the blockchain and mempool, so we synchronize this method
    private synchronized void receivedBlock(Block block){
        if(localBlockchain.isEmpty()){
            System.out.println("Node " + nodeId + " has no genesis block, cannot accept new blocks.");
            return;
        }
        Block tip = localBlockchain.getLast();
        if(!block.previousHash.equals(tip.hash)){
            System.out.println("Node " + nodeId + " rejected block: previous hash does not match our tip.");
            System.out.println("Expected: " + tip.hash.substring(0,12));
            System.out.println("Received: " + block.previousHash.substring(0,12));
            return;
        }

        // Block hash must be valid for its difficulty
        String target = new String(new char[block.difficulty]).replace('\0', '0');
        if(!block.hash.substring(0, block.difficulty).equals(target)){
            System.out.println("Node " + nodeId + " rejected block: hash does not meet difficulty target.");
            return;
        }

        // Recalculate hash must match stored hash
        if(!block.hash.equals(block.calculateHash())){
            System.out.println("Node " + nodeId + " rejected block: hash does not match calculated hash.");
            return;
        }

        // Verify all transaction signature
        for(int t = 0; t < block.transactions.size(); t++){
            Transaction tx = block.transactions.get(t);
            if(t == 0 && tx.sender == null) continue;
            if(!tx.verifySignature()){
                System.out.println("Node " + nodeId + " rejected block: transaction " + t + " has invalid signature.");
                return;
            }
        }

        // If we reach this point, the block is valid and extends our chain, so we can add it
        localBlockchain.add(block);

        // Update local UTXO
        updateLocalUTXOs(block);
        System.out.println("Node " + nodeId + " accepted block: " + "local height: "+localBlockchain.size());
    }

    void updateLocalUTXOs(Block block){
        for(int t= 0; t < block.transactions.size(); t++){
            Transaction tx = block.transactions.get(t);
            for(TransactionOutput out: tx.outputs){
                localUTXOs.put(out.id, out);
            }
            if(t != 0){
                for(TransactionInput input: tx.inputs){
                    localUTXOs.remove(input.transactionOutputId);
                }
            }
        }
    }

    // this is called after node mines a block and sends the block to every known peer
    // Real bitcoin network propgation model: a node that mines a block broadcast to all 8 outbound peers simultaneiusly. Each peer validates and forwards to their peers.
    // Exponential propagation - a block typically reaches 50% of the network in 1 second
    public void broadcast(Block block){
        System.out.println("Node " + nodeId + " broadcasting block: " + peerAddresses.size());
        for(String peer: peerAddresses){
            new Thread(() -> sendToPeer(peer, block),nodeId + "broadcast").start();
        }
    }

    // Open a socket to one peer and streams the block
    private void sendToPeer(String address, Block block) {
        String[] parts = address.split("-");
        String host = parts[0];
        int peerPort = Integer.parseInt(parts[1]);

        int attempts = 0;
        while (attempts < 3) {
            try (Socket socket = new Socket(host, peerPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.writeObject(block);
                out.flush();
                System.out.println("Node " + nodeId + " successfully sent block to " + address);
                return;

            } catch (ConnectException e) {
                attempts++;
                System.out.println("Node " + nodeId + " failed to send block to " + address + " (attempt " + attempts + "): " + e.getMessage());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                    System.out.println("Node " + nodeId + " broadcast thread interrupted while waiting to retry.");
                    return;
                }
            } catch (IOException e) {
                System.out.println("Node " + nodeId + " failed to send block to " + address + " after 3 attempts, giving up.");

            }
        }
    }

        public String getNodeId () {
            return nodeId;
        }
        public int getLocalHeight () {
            return localBlockchain.size();
        }
        public Block getLocalChainTip () {
            if (localBlockchain.isEmpty()) return null;
            return localBlockchain.getLast();
        }
        public void printStatus () {
            System.out.println("Node " + nodeId + " status: height=" + localBlockchain.size() + ", peers=" + peerAddresses.size());
        }
    }






