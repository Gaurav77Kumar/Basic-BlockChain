import java.security.PublicKey;
import java.io.Serializable;

public class TransactionOutput implements Serializable {
    public static final long serialVersionUID = 1L;

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

    public boolean isMine(PublicKey publicKey) {
        return (publicKey.equals(recipient));
    }
}


