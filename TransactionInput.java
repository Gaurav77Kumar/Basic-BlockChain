import java.io.Serializable;

public class TransactionInput implements Serializable {
    private static final long serialVersionUID = 1L;
   /*
    UTXOs means Unspent Transaction Outputs. TransactionInput is referencing one of these outputs to indicate that the value is being used as an input in a new transaction.
    The transactionOutputId is the id of the TransactionOutput that is being referenced as an input. The UTXO field will hold the actual TransactionOutput object that is being referenced,
    which can be used to verify the transaction and ensure that the input value is valid and has not already been spent.
    */

    public String transactionOutputId;      // Reference to TransactionOutputs -> transactionId
    public TransactionOutput UTXO;          // Contains the Unspent transaction output

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

}
