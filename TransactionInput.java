import java.io.Serializable;

public class TransactionInput implements Serializable {
    private static final long serialVersionUID = 1L;


    public String transactionOutputId;     // Reference to TransactionOutputs -> transactionId
    public TransactionOutput UTXO;          // Contains the Unspent transaction output

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

}
