import java.security.Security;
import javax.swing.SwingUtilities;

// Set up genesis, wire wallets, call MiningEnginer and chainValidator

public class Noob{

    public static void main(String[] args) throws InterruptedException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // Setup Wallets

        BlockchainState.walletA = new Wallet();
        BlockchainState.walletB = new Wallet();
        Wallet coinbase = new Wallet();

        // 100 coins hoga to start the chain
        BlockchainState.genesisTransaction = new Transaction(coinbase.publicKey, BlockchainState.walletA.publicKey, 100_00000000L,0L,null);

        BlockchainState.genesisTransaction.generateSignature(coinbase.privateKey);
        BlockchainState.genesisTransaction.transactionId = "0";
        BlockchainState.genesisTransaction.outputs.add(new TransactionOutput(BlockchainState.genesisTransaction.recipient, BlockchainState.genesisTransaction.value, BlockchainState.genesisTransaction.transactionId));
        BlockchainState.UTXOs.put(BlockchainState.genesisTransaction.outputs.get(0).id, BlockchainState.genesisTransaction.outputs.get(0));


        System.out.println("Creating and Mining Genesis block... ");
        Block genesis = new Block("0");
        genesis.addTransaction(BlockchainState.genesisTransaction);
        genesis.mineBlock(BlockchainState.difficulty);
        BlockchainState.blockchain.add(genesis);

        NetworkManager network = new NetworkManager();

        Node node1 = network.createNode(" Node 1", 6001);
        Node node2 = network.createNode("node 2",6002);

        network.connectPeers(node1, node2, "localhost");

        network.seedAll(genesis,BlockchainState.UTXOs);

        network.startAll();
        Thread.sleep(200);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            network.stopAll();
        }));

        SwingUtilities.invokeLater(() -> new Dashboard(node1, node2));


        BlockchainState.printState();
        network.printNetworkStatus();

        System.out.println("Wallet -> WalletB: 40 coins");
        BlockchainState.walletA.sendFunds(
                BlockchainState.walletB.publicKey,
                40_00000000L,
                50_000_000L
        );

        System.out.println("Node 1 mines block 1 and broadcast");
        Block b1 = MiningEngine.mineNextBlock(genesis.hash, BlockchainState.walletA, node1);
        Thread.sleep(1000);

        network.printNetworkStatus();

        System.out.println("WalletB -> WalletA: 20 coins");
        BlockchainState.walletB.sendFunds(
                BlockchainState.walletA.publicKey,
                20_00000000L,
                20_000_000L
        );

        System.out.println("Node 2 mines block 2 and broadcast");
        Block b2 = MiningEngine.mineNextBlock(b1.hash, BlockchainState.walletB, node2);
        Thread.sleep(1000);


        Block b3 = MiningEngine.mineNextBlock(b2.hash, BlockchainState.walletA, node1);
        Thread.sleep(1000);
        System.out.println("Waiting for all broadcast to complete");
        Thread.sleep(3000);


        network.printNetworkStatus();
        BlockchainState.printState();


        ChainValidator.isChainValid();

    }
}

