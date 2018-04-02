import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Random;


public class User {
	private String name;
	private ArrayList<Transaction> transactions;
	private Ledger ledger;
	private PrivateKey privateKey;
	private PublicKey publicKey;

	public User(String name){
		this.name = name;
		this.transactions = new ArrayList<Transaction>();
		ledger = new Ledger();
		generateKeys();

	}
	private void generateKeys(){
		KeyPairGenerator kg;
		try {
			kg = KeyPairGenerator.getInstance("DSA");
			KeyPair keyPair = kg.genKeyPair();
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private byte[] signString(String content) throws Exception{
	    Signature sign = Signature.getInstance("DSA");
	    sign.initSign(privateKey);
	    sign.update(content.getBytes());
	    return sign.sign();
	}
	public boolean verifySignature(byte[]signature,String content,PublicKey pk) throws Exception{
	    Signature sign = Signature.getInstance("DSA");
	    sign.initVerify(pk);
	    sign.update(content.getBytes());
		return sign.verify(signature);
	}
	public PublicKey getPublicKey(){
		return publicKey;
	}
	
	public int randomInt(int min, int max){
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	public ArrayList<User> selectTargetPeers(){
		System.out.println("User " + this);
		ArrayList<User> nearPeers = Main.networkGraph.get(this.getName());
		System.out.println("User Peers" + this + " "  + nearPeers);

		ArrayList<User> targets = new ArrayList<>();
		int targetsCount = randomInt(0, nearPeers.size());

		for (int i = 0; i < targetsCount; i++) {
			int targetIndex = randomInt(0, nearPeers.size()-1);
			targets.add(nearPeers.get(targetIndex));
			nearPeers.remove(targetIndex);
		}

		return targets;
	}
	private PublicKey getOriginatorPublicKey(Transaction transaction){
		String originatorName=transaction.getOriginator();
		for(User u:Main.usersList){
			if(u.name.equals(originatorName)){
				return u.getPublicKey();
			}
		}
		return null;
	}
	public void announceTransaction(Transaction transaction) throws Exception{
		PublicKey pk=getOriginatorPublicKey(transaction);
		System.out.println("Authenticating transaction first before announcing to peers");
		if(verifySignature(transaction.getSignature(),transaction.getContent(),pk)){
			System.out.println("Authentication sucessful will forward to peers");
		ArrayList<User> targets = selectTargetPeers();
		System.out.println(name + " : Announcing transaction " + transaction.getId() + " to " + targets);
		
		for(User current : targets){
			current.receiveTransaction(transaction);
		}
		}
		else{
			System.out.println("Authentication failed therfore will not forward to peers");
		}
	}

	public Transaction generateTransaction() throws Exception{
		String rdmStr=generateRandomString(20);
		byte[]sig=signString(rdmStr);
//		System.out.println(verifySignature(sig,rdmStr,publicKey));
		Transaction t= new Transaction(Main.currentTransactionId++, this.getName(),this.getName(), rdmStr,sig);
		transactions.add(t);
		if(transactions.size() == Main.blockSize){
			createBlock();
			transactions.clear();
			System.out.println("User "+name+" created a block and appended it to the ledger");
		}
		return t;
	}

	public static String generateRandomString(long length){
		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		while (salt.length() < length) {
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}

		String saltStr = salt.toString();
		return saltStr;
	}

	public void receiveTransaction(Transaction transaction) throws Exception{
		if(!transactions.contains(transaction)){
			// Receive and see if a block can be formed
			//System.out.println(name + " : received transaction " + transaction.getId() + " from " + transaction.getAnnouncer());
			transactions.add(transaction);

			if(transactions.size() == Main.blockSize){
				createBlock();
				transactions.clear();
				System.out.println("User "+name+" created a block and appended it to the ledger");
			}

			// Forward to some near peers
			transaction.setAnnouncer(this.getName());
			announceTransaction(transaction);
		}else{
			System.out.println("Transaction already in "+name+" so not added or announced from user "+name);
		}
	}
	public String generateNonce() throws NoSuchAlgorithmException{
		String dateTimeNameString = Long.toString(new Date().getTime())+name; //inorder to be very unique and assured not in ledger
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(dateTimeNameString.getBytes(StandardCharsets.UTF_8));
		String encoded = Base64.getEncoder().encodeToString(hash);
		return encoded;
	}
	public void createBlock(){
		try {
			boolean isContainsNonce=false;
			String nonce;
			do{
				nonce=generateNonce();
				isContainsNonce=ledger.containsNonce(nonce);
			}while(isContainsNonce);
			Block block = new Block(transactions,nonce);
			ledger.appendBlock(block);
			announceBlock(block);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	//TODO: announce block to random subset of peers due for M2
	public void announceBlock(Block block){

	}
	@Override
	public boolean equals(Object obj) {
		return ((User) obj).name.equals(name);
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Transaction> getTransactions() {
		return transactions;
	}
}
