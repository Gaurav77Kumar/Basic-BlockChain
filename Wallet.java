import java.security.*;
import java.security.spec.ECGenParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/*
 Wallet does three things->
 1. Identity it holds a pair of public and private keys.
 2. Calculate wealth(getBalance).
 3. SpendMyCoins(sendFunds).
 */

public class Wallet {
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();

    public Wallet(){
        Security.addProvider(new BouncyCastleProvider());
        generateKeyPair();
    }

    // Create Identity on the Blockchain by generating a public/private key pair using Elliptic Curve Cryptography (ECC).
    // ECC is chosen because it offers strong security with smaller key sizes compared to other algorithms like RSA, making it more efficient for blockchain applications.
    private void generateKeyPair() {
        try{
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    // Returns the balance of coins in this wallet by summing the values of all unspent transaction outputs (UTX0s) that belong to this wallet.
    // It iterates through the global list of UTX0s, checks if each UTX0 belong to this wallet (by comparing the public key), and if it does,
    // it adds the value of that UTX0 to the total balance. It also keeps track of the UTX0s that belong to this wallet in a local HashMap for future reference when creating transactions.
    public long getBalance() {
        long total = 0;

        for(Map.Entry<String, TransactionOutput> item: BlockchainState.UTXOs.entrySet()){
            TransactionOutput UTX0 = item.getValue();
            // Check if it belongs to this wallet by comparing the public key with the recipient of the UTX0. If it matches, it means this wallet owns that UTX0 and can use it as an input for future transactions.
            UTXOs.put(UTX0.id, UTX0);
            if(UTX0.isMine(publicKey)) {
                total += UTX0.value ;
            }
        }
        return total;
    }

    // Creates, signs, and submits a transaction if sufficient funds are available.
    public Transaction sendFunds(PublicKey _recipient, long value,long fee ) {
        long totalNeeded = value + fee;
        if(getBalance() < totalNeeded) {
            System.out.println("#Not Enough funds to send transaction."+
                    StringUtil.toCoins(getBalance()) +
                    " Need"+ StringUtil.toCoins(totalNeeded)
                    );
            return null;
        }

        // Select enough UTXOs to fund the transaction and fee.
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
        long total = 0;

        for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
            TransactionOutput UTX0 = item.getValue();
            total += UTX0.value;
            inputs.add(new TransactionInput(UTX0.id));
            if(total > totalNeeded) break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient, value,fee, inputs);
        newTransaction.generateSignature(privateKey);

        // Remove the used UTXOs from the wallet's list of UTXOs
        for(TransactionInput input: inputs){
            UTXOs.remove(input.transactionOutputId);
        }
        BlockchainState.mempool.add(newTransaction);
        System.out.println("Transaction submitted to mempool. pending: " +
                StringUtil.toCoins(value)+
                " Fee"+ StringUtil.toCoins(fee)+
                "mempool"+BlockchainState.mempool.size());
        return newTransaction;
    }

}
/*
 There is some inconsistency in this like local cache is updated immediately, global map is updated only when mined so if mining fails local cache thinks
 UTXOs are spent but the global map still has them. This can lead to issues where the wallet's balance is incorrect after a failed mining attempt.
 To address this, I will implement a mechanism to revert the local cache changes if the mining process fails, ensuring that the wallet's balance remains accurate.
 */