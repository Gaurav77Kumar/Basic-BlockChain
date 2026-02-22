import java.security.*;
import java.util.ArrayList;

public class Transaction {

    public String transactionId;       // This is also the hash of the transaction
    public PublicKey sender;           // Senders address/public key
    public PublicKey recipient;        // Recipients address/public key
    public float value;                // Contains the amount we wish to send to the recipient
    public byte[] signature;           // This is to prevent anybody else from spending funds in our

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();       // List of transaction inputs
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();     // List of transaction outputs

    private static int sequence = 0;      // A rough count of how many transactions have been generated

    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    // This will generate the transaction hash (which will be used as its id)
    private String calculateHash() {
        sequence++;                    // increase the sequence to avoid 2 identical transactions having the same hash

        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(recipient) +
                        Float.toString(value) + sequence
        );
    }

    // Signs all the data we don't wish to be tampered with.
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender ) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    // Verify the data we signed hasn't been tampered with
    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender ) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        return StringUtil.verifyECDSASig(sender,data, signature);
    }

    // Returns true if new transaction could be created.
    public boolean processTransaction() {

        if(verifySignature() == false) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        // Gather transaction inputs (Make sure they are unspent):
        for(TransactionInput i : inputs) {
            i.UTXO = Noob.UTXOs.get(i.transactionOutputId);
    }

        // Check if transaction is valid:
        if(getInputsValue() < Noob.minimumTransaction) {
            System.out.println("#Transaction Inputs too small: " + getInputsValue());
            return false;
        }

        // Generate transaction outputs:
        float leftOver = getInputsValue() - value; // get value of inputs then the left over change:
        transactionId = calculateHash();
        outputs.add(new TransactionOutput( this.recipient, value,transactionId)); // send value to recipient
        outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); // send the left over 'change' back to sender

        // Add outputs to Unspent list
        for(TransactionOutput o : outputs) {
            Noob.UTXOs.put(o.id , o);
        }

        // Remove transaction inputs from UTXO lists as spent:
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; // if Transaction can't be found skip it
            Noob.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    // returns sum of inputs(UTXOs) values
    public float getInputsValue() {
        float total = 0;
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue; //if Transaction can't be found skip it
            total += i.UTXO.value;
        }
        return total;
    }

    // returns sum of inputs(UTXOs) values
    public float getOutputsValue() {
        float total = 0;
        for (TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
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
            for(int i=1; i < previousTreeLayer.size(); i++) {
                treeLayer.add(StringUtil.applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }


}

// Signatures perform two very important task of blockchain:
// 1. They allow only the owner to spend their coins.
// 2. They protect the blockchain from hackers trying to change the transaction data (like amount) after it has been signed.
// If the data is changed after signing, the signature will be invalidated and the transaction will be rejected by the network.

// The Private key is used to sign the data and public key can be used to verify its integrity