import java.security.*;
import java.security.spec.ECGenParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Wallet {
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public HashMap<String, TransactionOutput> UTX0s = new HashMap<String, TransactionOutput>();  // Only UTX0s owned by this wallet.

    public Wallet(){
        Security.addProvider(new BouncyCastleProvider());
        generateKeyPair();
    }

    private void generateKeyPair() {
        try{
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            // Initialize the key generator and generate a keyPair
            keyGen.initialize(ecSpec, random);
            // Set the public and private keys from the keypair

            KeyPair keyPair = keyGen.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    // Return balance and stores the UTX0s owned by this wallet in this
    public float getBalance() {
        float total = 0;

        for(Map.Entry<String, TransactionOutput> item: Noob.UTXOs.entrySet()){
            TransactionOutput UTX0 = item.getValue();
            if(UTX0.isMine(publicKey)) {               // If output belongs to me
                UTX0s.put(UTX0.id, UTX0);              // Add it to our list of unspent transactions.
                total += UTX0.value ;
            }
        }
        return total;
    }

    // Generates and returns a new transaction from this wallet.
    public Transaction sendFunds(PublicKey _recipient, float value ) {
        if(getBalance() < value) {    // gather balance and check funds
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }

        // Create array list of inputs
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
        float total = 0;

        for (Map.Entry<String, TransactionOutput> item: UTX0s.entrySet()){
            TransactionOutput UTX0 = item.getValue();
            total += UTX0.value;
            inputs.add(new TransactionInput(UTX0.id));
            if(total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey,_recipient,value,inputs);
        newTransaction.generateSignature(privateKey);

        for(TransactionInput input: inputs){
            UTX0s.remove(input.transactionOutputId);
        }
        return newTransaction;
    }

}
