import java.util.ArrayList;
import java.util.HashMap;

// Globally accessible without passing any reference for this project this is good idea but for production we use dependency injection or Service Allocator
public class BlockchainState{

    public static ArrayList<Block> blockchain = new ArrayList<>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    public static ArrayList<Transaction> mempool = new ArrayList<>();

    public static int difficulty = 3;
    public static int retargetInterval = 3;
    public static long targetBlockTimeMs = 2000;

    public static long miningReward = 10_00000000L;
    public static long minimumTransaction = 1_000_000L;

    public static Wallet walletA;
    public static Wallet walletB;

    public static Transaction genesisTransaction;

    // Initialize the blockchain state with two wallets and a genesis transaction that gives walletA some coins to start with.
    public static Block getChainTip(){
        if(blockchain.isEmpty()) return null;
        return blockchain.get(blockchain.size()-1);
    }

    public static int getHeight(){
        return blockchain.size() ;
    }

    public static void printState(){
        System.out.println("Chain height: "+ getHeight());
        System.out.println("Difficulty: "+difficulty);
        System.out.println("Mempool: " + mempool.size() + "pending TX");
        System.out.println("Wallet A: " + StringUtil.toCoins(walletA.getBalance()));
        System.out.println("wallet B: "+ StringUtil.toCoins(walletB.getBalance()));
    }
}
