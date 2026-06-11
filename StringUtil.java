import java.security.Key;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.security.PublicKey;

public class StringUtil {
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");  // SHA-256 chosen because it is widely used and considered secure. It produces a 256-bit (32-byte) hash value, which is typically rendered as a hexadecimal string of 64 characters.
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();                        // StringBuilder is used instead of String because if we use String we have to create different String object for every iteration and then concat them, which is costly. StringBuilder is more efficient.

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    // This method is used to Sign data using ECDSA and returns the result ( as bytes ).
    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        Signature dsa;
        // Signature is java built in class for cryptographic signing and verification

        byte[] output = new byte[0];
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            byte[] realSig = dsa.sign();
            output = realSig;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    // Verifies a String signature by taking publicKey, data and Signature
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    // It returns the raw key bytes and encodes them as readable Base64 string that can be easily stored or transmitted.
    // Base64 encoding is a common way to represent binary data in a text format, making it suitable for use in various applications such as JSON, XML, or simply for display purposes.
    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    // This method converts a long value representing Satoshi  (the smallest unit of Bitcoin) into a human-readable string format with 8 decimal places, which is the standard way to represent Bitcoin amounts.
    // For example, if you have 100000000 satoshis, it will return "1.00000000", which represents 1 Bitcoin.
    public static String toCoins(long satoshis){
        return String.format("%.8f", satoshis/100000000.0);
    }


}