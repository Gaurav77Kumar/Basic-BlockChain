import java.io.Serializable;
import java.security.*;
import java.util.ArrayList;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    public String transactionId;
    public PublicKey sender;
    public PublicKey recipient;
    public long value;
    public long fee;
    public byte[] signature;

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static int sequence = 0;

    public Transaction(PublicKey from, PublicKey to, long value,long fee, ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
        this.fee = fee;
    }

    // This will generate the transaction hash (which will be used as its id)
    private String calculateHash() {
        sequence++;
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(recipient) +
                        Long.toString(value) +
                        Long.toString(fee)+
                        sequence
        );
    }

    // Signs all the data we don't wish to be tampered with.
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender ) + StringUtil.getStringFromKey(recipient) +
                Long.toString(value) +
                Long.toString(fee);
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    // Verify the data we signed hasn't been tampered with
    public boolean verifySignature() {

        if(sender == null) return true;
        String data = StringUtil.getStringFromKey(sender )
                     + StringUtil.getStringFromKey(recipient)
                     + Long.toString(value)
                     + Long.toString(fee);

        return StringUtil.verifyECDSASig(sender,data, signature);
    }

    // Returns true if new transaction could be created.
    public boolean processTransaction() {

        if(!verifySignature()) {
            System.out.println("Transaction Signature failed to verify");
            return false;
        }

        // Gather transaction inputs (Make sure they are unspent):
        for(TransactionInput i : inputs) {
            i.UTXO = BlockchainState.UTXOs.get(i.transactionOutputId);
    }

        // Check if transaction is valid:
        if(getInputsValue() < BlockchainState.minimumTransaction) {
            System.out.println("#Transaction Inputs too small: " + StringUtil.toCoins((long) getInputsValue()));
            return false;
        }

        // Generate transaction outputs:
        long leftOver = (long) (getInputsValue() - value -fee);

        if(leftOver < 0){
            System.out.println("Insufficient funds to cover value + fee");
            return false;
        }
        transactionId = calculateHash();
        outputs.add(new TransactionOutput( this.recipient, value,transactionId));
        outputs.add(new TransactionOutput( this.sender, leftOver,transactionId));

        // Add outputs to Unspent list
        for(TransactionOutput o : outputs) {
            BlockchainState.UTXOs.put(o.id , o);
        }

        // Remove transaction inputs from UTXO lists as spent:
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue;
            BlockchainState.UTXOs.remove(i.UTXO.id);
        }
        return true;
    }

    // returns sum of inputs(UTXOs) values
    public long getInputsValue() {
        long total = 0;
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue;
            total += i.UTXO.value;
        }
        return total;
    }

    // returns sum of outputs
    public long getOutputsValue() {
        long total = 0;
        for (TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }

    public long getActualFee(){
        return getInputsValue() - getOutputsValue();
    }

    // Tacks in array of transactions and returns a merkle root.
    public static String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();

        ArrayList<String> previousTreeLayer = new ArrayList<String>();

        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        ArrayList<String> treeLayer = previousTreeLayer;

        while(count > 1) {
            treeLayer = new ArrayList<String>();

            for(int i = 0; i < previousTreeLayer.size(); i += 2) {
                String left = previousTreeLayer.get(i);
                String right;

                if(i + 1 < previousTreeLayer.size()) {
                    right = previousTreeLayer.get(i + 1);
                } else {
                    right = left;
                }
                treeLayer.add(StringUtil.applySha256(left + right));
            }

            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }
}
