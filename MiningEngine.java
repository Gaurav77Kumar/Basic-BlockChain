import java.util.ArrayList;

public class MiningEngine {
    public static Block mineNextBlock(String previousHash, Wallet miner, Node miningNode){
        Block block = new Block(previousHash);
        sortMempoolByFee();
        long totalFees = calculateTotalFees();

        long totalReward = BlockchainState.miningReward + totalFees;

        Transaction coinbase = createCoinbaseTx(miner, totalReward);
        block.addCoinbase(coinbase);

        if(BlockchainState.mempool.isEmpty()){
            System.out.println("Mempool empty, only Coinbase in block");;
        } else{
            System.out.println("Packing block with " + BlockchainState.mempool.size() + "Tx sorted by fee");

            for(Transaction tx: BlockchainState.mempool) {
                System.out.println("Tx fee: " + StringUtil.toCoins(tx.fee) + " Value" + StringUtil.toCoins(tx.value));
                block.addTransaction(tx);
            }
                BlockchainState.mempool.clear();
            }
            commitBlock(block, miningNode);
            System.out.println("Block mined and added to blockchain"+ BlockchainState.getHeight()+ "mined | reward: "+ StringUtil.toCoins(totalReward)+ "Tx"+block.transactions.size());
            return block;
        }

        private static Transaction createCoinbaseTx(Wallet miner, long totalReward){
            Transaction coinbaseTx = new Transaction(null, miner.publicKey, totalReward, 0L,new ArrayList<>());
            coinbaseTx.transactionId = StringUtil.applySha256("COINBASE"+miner.publicKey.toString() + System.currentTimeMillis());
            TransactionOutput out = new TransactionOutput(miner.publicKey,totalReward, coinbaseTx.transactionId);

            coinbaseTx.outputs.add(out);
            BlockchainState.UTXOs.put(out.id, out);

            System.out.println("Created Coinbase transaction rewarding miner: "+StringUtil.toCoins(totalReward)+"coins miner(reward + fees");
            return coinbaseTx;
        }

        private static void sortMempoolByFee(){
        BlockchainState.mempool.sort((a,b) -> Long.compare(b.fee, a.fee));   // not b.fee - a.fee because subtraction overflow for large values
    }
        private static long calculateTotalFees(){
             return BlockchainState.mempool.stream().mapToLong(tx -> tx.fee).sum();   // Stream + maptoLong + sum is idiomatic is java way to sum a long field across collection
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