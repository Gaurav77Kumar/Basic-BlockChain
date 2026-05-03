import java.util.ArrayList;
import java.security.Security;
import java.util.HashMap;
import javax.swing.SwingUtilities;
import java.security.PublicKey;

public class Noob {

    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>(); //list of all unspent transactions.

    public static ArrayList<Transaction> mempool = new ArrayList<Transaction>();  // we are using mempool for unconfirmed transaction


    public static int difficulty = 1;
    public static float minimumTransaction = 0.1f;

   public static float miningReward = 10f;


    public static Wallet walletA;
    public static Wallet walletB;
    public static Transaction genesisTransaction;

    public static void main(String[] args) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // Create the new wallets
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        System.out.println("Coinbase key:" + coinbase.publicKey);
        System.out.println("walletA key: " + walletA.publicKey);

        // Create genesis transaction, which sends 100 NoobCoin to walletA;
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinbase.privateKey);     // manually sign the genesis transaction
        genesisTransaction.transactionId = "0"; // manually set the transaction id

        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId)); // manually add the Transactions Output
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        System.out.println("Creating and Mining Genesis block... ");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        SwingUtilities.invokeLater(Dashboard::new);

       // WalletA is miner now they will earn rewards for mining the block
        System.out.println("\n WalletA balance: " + walletA.getBalance());

        System.out.println("\n WalletA send 40 to walletB..");
        walletA.sendFunds(walletB.publicKey, 40f);

        // WalletA mines earn 10 coin reward automatically
        System.out.println("\nWalletA mines block 1 earn reward..");
        Block block1 = mineNextBlock(genesis.hash, walletA);

        System.out.println("\n WalletA balance: " + walletA.getBalance());
        System.out.println("\n WalletB balance: " + walletB.getBalance());

        System.out.println("\nWalletB send 20 to walletA..");
        walletB.sendFunds(walletA.publicKey, 20f);

        // walletB mines block 2 earn 10 coins
        System.out.println("\nWalletB mines block 2 earn reward..");
        Block block2 = mineNextBlock(block1.hash, walletB);

        System.out.println("\nWalletA balance: " + walletA.getBalance());
        System.out.println("\nWalletB balance: " + walletB.getBalance());



        isChainValid();
    }

    // Creating a new coinbase transaction no inputs creating coins from nothing and sending to miner
    public static Transaction createCoinbaseTx(Wallet miner){
        Transaction coinbaseTx = new Transaction(null,miner.publicKey, miningReward, new ArrayList<>());

        coinbaseTx.transactionId = StringUtil.applySha256(
                "COINBASE"+ miner.publicKey.toString() + System.currentTimeMillis()
        );

        TransactionOutput out = new TransactionOutput(
                miner.publicKey,
                miningReward,
                coinbaseTx.transactionId
        );
        coinbaseTx.outputs.add(out);
        UTXOs.put(out.id,out);

        System.out.println("Coinbase reward: " + miningReward + "coins> miner");
        return coinbaseTx;
    }

    // Miner drains mempool into new block and mines it
    public static Block mineNextBlock(String previousHash, Wallet miner) {
        Block block = new Block(previousHash);

        // coinbase is te first transaction in real bitcoin
        Transaction coinbase = createCoinbaseTx(miner);
        block.addCoinbase(coinbase);

        if (mempool.isEmpty()) {
            System.out.println("Mempool is empty nothing to mine");
            return null;
        }

        // pulling every pending transaction into this block
        for (Transaction tx : mempool) {
            block.addTransaction(tx);
        }
        mempool.clear();   // Empty the waiting room
        addBlock(block);

        System.out.println("Block mined with " + block.transactions.size() + " transactions(s)");
        return block;
    }


    public static Boolean isChainValid() {

        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        //loop through blockchain to check hashes:
        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            //compare registered hash and calculated hash:
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
            //check if hash is solved
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't been mined");
                return false;
            }
            TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if(t == 0 && currentTransaction.sender == null){
                    for(TransactionOutput output: currentTransaction.outputs){
                        tempUTXOs.put(output.id, output);
                    }
                    continue;
                }

                if (!currentTransaction.verifySignature()) {
                    System.out.println("Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }
                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("Inputs are not equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for (TransactionInput input : currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if (tempOutput == null) {
                        System.out.println("Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if (input.UTXO.value != tempOutput.value) {
                        System.out.println("Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for (TransactionOutput output : currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }
                if (currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
                    System.out.println("Transaction(" + t + ") output recipient is not who it should be");
                    return false;
                }
            }

        }
        System.out.println("Blockchain is valid");
        return true;
    }
    private static void addBlock(Block newBlock)  {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}