import java.security.PublicKey;

public class TransactionOutput {
    public String id;
    public PublicKey recipient;
    public long value;
    public String parentTransactionId;

    public TransactionOutput(PublicKey recipient, long value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(recipient)+Long.toString(value)+parentTransactionId);
    }

    // Check if coin belongs to you
    public boolean isMine(PublicKey publicKey) {
        return (publicKey.equals(recipient));
    }
}


