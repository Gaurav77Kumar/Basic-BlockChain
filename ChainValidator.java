import java.util.ArrayList;
import java.util.HashMap;

// Validate chain Integrity
// Read BlockchainState and Return boolean and failure reason


public class ChainValidator {

    public static boolean isChainValid(){

        if(BlockchainState.blockchain.size() < 2){
            System.out.println("Chain too short to validate, only genesis block exists");
            return true;
        }

        // We replay the whole chain and validate each block and transaction
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
        tempUTXOs.put(BlockchainState.genesisTransaction.outputs.get(0).id, BlockchainState.genesisTransaction.outputs.get(0));


        for(int i  = 1; i < BlockchainState.blockchain.size(); i++){
            Block current = BlockchainState.blockchain.get(i);
            Block previous = BlockchainState.blockchain.get(i-1);

            // Check 1: Stores hash matches recalculated hash
            if(!current.hash.equals(current.calculateHash())){
                fail("Hash mismatch", i, -1);
                return false;
            }

            // Check 2: Previous hash chain is intact
            if(!previous.hash.equals(current.previousHash)){
                fail("chain breal previous hash mismatch",i,-1);
                return false;
            }

            // Check 3: Block was mined at its own stored difficulty
            String target = new String(new char[current.difficulty]).replace('\0','0');

            if(!current.hash.substring(0,current.difficulty).equals(target)){
                fail("Block not mined correctly",i,-1);
                return false;
            }

            // Check 4: Validate every transaction in the block
            for(int t = 0; t < current.transactions.size(); t++){
                Transaction tx = current.transactions.get(t);

                // we have index 0 in Coinbase
                if( t == 0 && tx.sender == null){
                    for(TransactionOutput out: tx.outputs){
                        tempUTXOs.put(out.id,out);
                    }
                    continue;
                }

                // Signature must be valid
                if(!tx.verifySignature()){
                    fail("Invalid Signature",i,t);
                    return false;
                }

                // Inputs = Output + fees(Bitcoin conservative law
                if(tx.getInputsValue() != tx.getOutputsValue() + tx.fee){
                    fail("Input/output/fee mismatch", i,t);
                    return false;
                }

                // Input must reference a real unspent output
                for(TransactionInput input : tx.inputs){
                    TransactionOutput referenced = tempUTXOs.get(
                            input.transactionOutputId
                    );

                    if(referenced == null){
                        fail("UTXO not found ",i,t);
                        return false;
                    }
                    if(input.UTXO.value != referenced.value){
                        fail("UTXO value tampered",i,t);
                        return false;
                    }
                    tempUTXOs.remove(input.transactionOutputId);
                }

                // Add new Output to temp UTXO set
                for(TransactionOutput out: tx.outputs){
                    tempUTXOs.put(out.id,out);
                }
            }
        }
        System.out.println("Blockchain is valid"+BlockchainState.blockchain.size()+"block checked");
        return true;

    }

    private static void fail(String reason, int blockIndex, int txIndex){
        if(txIndex >= 0){
            System.out.println("Block "+blockIndex+" Transaction "+txIndex+" failed validation: "+reason);
        } else {
            System.out.println("Block "+blockIndex+" failed validation: "+reason);
        }
    }
}
