import java.util.ArrayList;

// Create and Mine Blocks

public class MiningEngine {
    // This is main entry point call this to mine a new block
    // Handles Coinbase creation, mempool drain and pow and difficulty adjustment

    public static Block mineNextBlock(String previousHash, Wallet miner, Node miningNode){
        Block block = new Block(previousHash);

        // Step 1: sort mempool -highest fee first
        sortMempoolByFee();

        // Step 2: Calculate total fees to award miner
        long totalFees = calculateTotalFees();
        long totalReward = BlockchainState.miningReward + totalFees;

        // Step 3: Coinbase is always the first TX in a block
        Transaction coinbase = createCoinbaseTx(miner, totalReward);
        block.addCoinbase(coinbase);

        // Step 4: Drain mempool into block until block is full or mempool is empty

        if(BlockchainState.mempool.isEmpty()){
            System.out.println("Mempool empty, only coinbase in block");
        } else{
            System.out.println("Packing block with " + BlockchainState.mempool.size() + "Tx sorted by fee");
            for(Transaction tx: BlockchainState.mempool) {
                System.out.println("Tx fee: " + StringUtil.toCoins(tx.fee) + " Value" + StringUtil.toCoins(tx.value));
                block.addTransaction(tx);
            }
                BlockchainState.mempool.clear();
            }

            // Step 5: mine + append + retarget difficulty
            commitBlock(block, miningNode);
            System.out.println("Block mined and added to blockchain"+ BlockchainState.getHeight()+ "mined | reward: "+ StringUtil.toCoins(totalReward)+ "Tx"+block.transactions.size());
            return block;
        }

        // Create Coinbase transaction to reward miner this is only legal way new coins enter the system
        private static Transaction createCoinbaseTx(Wallet miner, long totalReward){
            Transaction coinbaseTx = new Transaction(
                    null, miner.publicKey, totalReward, 0L,new ArrayList<>()
            );
            coinbaseTx.transactionId = StringUtil.applySha256(
                    "COINBASE"+miner.publicKey.toString() + System.currentTimeMillis()
            );
            TransactionOutput out = new TransactionOutput(
                    miner.publicKey,totalReward, coinbaseTx.transactionId
            );
            coinbaseTx.outputs.add(out);
            BlockchainState.UTXOs.put(out.id, out);
            System.out.println("Created coinbase transaction rewarding miner: "+StringUtil.toCoins(totalReward)+"coins miner(reward + fees");
            return coinbaseTx;
        }

        // Sort mempool by fee highest pehle kyuki it is how world work
        private static void sortMempoolByFee(){
        BlockchainState.mempool.sort((a,b) -> Long.compare(b.fee, a.fee));
    }
    // Sums fees across all pending Transaction
    private static long calculateTotalFees(){
        return BlockchainState.mempool.stream().mapToLong(tx -> tx.fee).sum();
    }

    // Mines the block then append to chain + trigger difficulty retarget if needed
    private static void commitBlock(Block block, Node miningNode){
        block.mineBlock(BlockchainState.difficulty);
        BlockchainState.blockchain.add(block);



        miningNode.localBlockchain.add(block);
        miningNode.updateLocalUTXOs(block);

        miningNode.broadcast(block);


        adjustDifficulty();
    }

    // Retarget difficulty for next block
    private static void adjustDifficulty(){
        int size = BlockchainState.blockchain.size();
        if(size == 0 || size % BlockchainState.retargetInterval != 0) return;

        Block latest = BlockchainState.blockchain.get(size - 1);
        Block reference = BlockchainState.blockchain.get(size - BlockchainState.retargetInterval);

        long actualMs = latest.timeStamp - reference.timeStamp;
        long expectedMs = BlockchainState.targetBlockTimeMs + BlockchainState.retargetInterval;

        System.out.println("Difficulty retarget");
        System.out.println("Expected: " + expectedMs + "ms | Actual: "+actualMs+"ms");

        if(actualMs < expectedMs / 2){
            BlockchainState.difficulty++;
            System.out.println("Toos fast  Difficulty: "+BlockchainState.difficulty);
        } else if (actualMs > expectedMs * 2){
            BlockchainState.difficulty = Math.max(1, BlockchainState.difficulty);
            System.out.println("Too slow Difficulty: "+BlockchainState.difficulty);

        } else{
            System.out.println("Difficulty stays the same: "+BlockchainState.difficulty);
        }


    }
}