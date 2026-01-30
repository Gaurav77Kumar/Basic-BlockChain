import java.util.Date;


public class Block {

    public String hash;
    public String previousHash;
    private String data;               // our data will be a simple message.
    private long timeStamp;            // as number of milliseconds since 1/1/1970

    private int nonce;

    // Block Constructor.
    public Block(String data, String previoushash){
        this.data = data;
        this.previousHash = previoushash;
        this.timeStamp = new Date().getTime();     // this will create a timestamp for us like 13121 that is helpful to us
        this.hash = calculateHash();
    }

    public String calculateHash() {
        String calculatehash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) + // You MUST add this!
                        data
        );
        return calculatehash;
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0"
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

}

