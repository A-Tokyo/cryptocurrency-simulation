import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Random;

import cipher.CipherHelpers;

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
		//uncomment this segment to have users with public key modulus and exponent as name //TODO: uncomment
//		RSAPublicKey rspk=(RSAPublicKey)  publicKey;
//		this.name=rspk.getModulus().toString()+rspk.getPublicExponent().toString();
	}

	public void appendToLogs(String text) throws IOException{
		text = "\n" + text + "\n";
		Files.write(Paths.get("logs.txt"), text.getBytes(), StandardOpenOption.APPEND);
	}

	public void generateKeysDSA(){
		KeyPairGenerator kg;
		try {
			kg = KeyPairGenerator.getInstance("DSA");
			KeyPair keyPair = kg.genKeyPair();
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
		} 
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public byte[] signStringDSA(String content) throws Exception{
		Signature sign = Signature.getInstance("DSA");
		sign.initSign(privateKey);
		sign.update(content.getBytes());
		return sign.sign();
	}

	public boolean verifySignatureDSA(byte[]signature,String content,PublicKey pk) throws Exception{
		Signature sign = Signature.getInstance("DSA");
		sign.initVerify(pk);
		sign.update(content.getBytes());
		return sign.verify(signature);
	}

	public PublicKey getPublicKeyDSA(){
		return publicKey;
	}

	private void generateKeys(){ //RSA
		try {
			KeyPair keyPair = CipherHelpers.generateKeyPair();
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
		} 
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	private byte[] signString(String content) throws Exception{
		Signature sign = Signature.getInstance("SHA256withRSA");
		sign.initSign(privateKey);
		sign.update(content.getBytes());
		return sign.sign();
	}

	public boolean verifySignature(byte[]signature,String content,PublicKey pk) throws Exception{
		Signature sign = Signature.getInstance("SHA256withRSA");
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

	//	// It lets a user solve a complex puzzle and returns true if the user managed to solve it and false otherwise
	//	public boolean solvedComplexPuzzle(){ //not needed since will directly call the other function when finishes puzzle
	//		return false;
	//	}

	public ArrayList<User> selectTargetPeers(){
		ArrayList<User> nearPeers = Main.networkGraph.get(this.getName());
		ArrayList<User> nearPeersCopy = new ArrayList<>();
		nearPeersCopy.addAll(nearPeers);

		ArrayList<User> targets = new ArrayList<>();
		int targetsCount = randomInt(1, nearPeers.size());

		for (int i = 0; i < targetsCount; i++) {
			int targetIndex = randomInt(0, nearPeersCopy.size()-1);
			targets.add(nearPeersCopy.get(targetIndex));
			nearPeersCopy.remove(targetIndex);
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
		ArrayList<User> targets = selectTargetPeers();
		appendToLogs(name + " : Announcing transaction " + transaction.getId() + " to " + targets);

		for(User current : targets){
			current.receiveTransaction(transaction);
		}
	}

	public Transaction generateTransaction() throws Exception{
		String rdmStr = generateRandomString(20);
		byte [] sig = signString(rdmStr);
		Transaction t = new Transaction(this.getName(), this.getName(), rdmStr, sig);
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
			PublicKey pk = getOriginatorPublicKey(transaction);
			appendToLogs("User "+name+" authenticating transaction "+transaction.getId() +" first before accepting and announcing to peers");
			System.out.print(".");
			if(verifySignature(transaction.getSignature(),transaction.getContent(),pk)){
				appendToLogs("Authentication successful, user "+name+ " will add transaction "+transaction.getId() +" and forward to peers");
				transactions.add(transaction);

				if(transactions.size() == Ledger.getBlocksize()){
					createBlock();
					transactions.clear();
					appendToLogs("User "+name+" created a block and appended it to the ledger");
				}

				// Forward to some near peers
				transaction.setAnnouncer(this.getName());
				announceTransaction(transaction);
			}
			else{
				appendToLogs("Authentication failed therfore user "+name +" will not forward to peers transaction"+transaction.getId());
			}
		}
		else{
			appendToLogs("Transaction " + transaction.getId() + " already in "+name+" so not added or announced from user "+name);
		}
	}

	public String generateNonce() throws NoSuchAlgorithmException{
		String dateTimeNameString = Long.toString(new Date().getTime())+name; //in-order to be very unique and assured not in ledger
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(dateTimeNameString.getBytes(StandardCharsets.UTF_8));
		String encoded = Base64.getEncoder().encodeToString(hash);
		return encoded;
	}

	public String generateHash(String nonce) throws NoSuchAlgorithmException{
		String hashStr= transactions.toString()+ledger.lastBlock().getHash()+nonce; //TODO: handle genesis block
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(hashStr.getBytes(StandardCharsets.UTF_8));
		String encoded = Base64.getEncoder().encodeToString(hash);
		return encoded;
	}
	//verify that the hashing of block is consistent to what the block contains
	public Boolean verifyBlockHash(Block block){
		String hashStr=block.getTransactions().toString()+block.getPrevBlock().getHash()+block.getNonce(); //TODO: handle genesis block
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(hashStr.getBytes(StandardCharsets.UTF_8));
			String encoded = Base64.getEncoder().encodeToString(hash);
			return block.getHash().equals(encoded);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}
	public void createBlock() throws IOException{
		try {
			boolean isContainsNonce=false;
			String nonce;
			do{
				nonce=generateNonce();
				isContainsNonce=ledger.containsNonce(nonce);
			}
			while(isContainsNonce || !nonce.substring(0, 2).equals("00")); //make sure starts with 00 and not in ledger before (puzzle)
			String hash=generateHash(nonce);
			Block block = new Block(transactions,nonce,hash);
			ledger.appendBlock(block);
			announceBlock(block);
		} 
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void announceBlock(Block block) throws IOException{
		// If managed to solve a complex puzzle, a user can then announce a block
		//		if(solvedComplexPuzzle()){ //separate function not needed since if complex puzzle is solved then announceBlock function gets called
		ProposedBlock proposedBlock = new ProposedBlock(block,this);
		ArrayList<User> randomTargetPeers = selectTargetPeers();
		for(User peer : randomTargetPeers){
			peer.handleProposedBlock(proposedBlock);
		}
		//		}
	}

	public void handleProposedBlock(ProposedBlock proposedBlock) throws IOException{
		String proposerName = proposedBlock.proposer.getName();
		appendToLogs("user "+name + " recieved a block from user "+ proposerName);
		appendToLogs("user "+name + " verifying the block first ");
		if(verifyBlockHash(proposedBlock)){ 	//verify that the hashing of block is consistent with the contents of the block first, if not it will be ignored
			appendToLogs("user "+name + " sucessfully verified the recieved block's content and hash since computed hash and the stated block's hash are the same");
			// Users do not vote for blocks they proposed
			if(proposerName.equals(name)){
				appendToLogs(name + " : Cannot vote since I proposed this block");
				return;
			}

			// Users do not vote for blocks they voted for previously 
			if(proposedBlock.uniqueVoters.contains(name)){
				appendToLogs(name + " : Cannot vote since I already voted for this block proposed by " + proposerName);
				return;
			}

			proposedBlock.uniqueVoters.add(name);
			if(proposedBlock.uniqueVoters.size() == Main.usersCount){
				// All users -except the proposer- voted for the block
				if(proposedBlock.confirmations > proposedBlock.rejections){
					appendToLogs(proposerName + " : My block is accepted");
					proposedBlock.proposer.appendBlock(proposedBlock);
					Main.ledger.appendBlock(proposedBlock);
					Main.updateUsersLedgers();
					// @Mamdouh >>>>>>>>>>>>>> Update all users with the new block
				}
				else{
					appendToLogs(proposerName + " : My block is orphaned");
					// @Tokyo >>>>>>>>>>>>>> Handle orphaned block
				}
			}
			else{
				if(ledger.canBeAppended(proposedBlock))
					proposedBlock.confirmations++;
				else
					proposedBlock.rejections++;


				// After voting, pass the block to a random set of near peers
				ArrayList<User> randomTargetPeers = selectTargetPeers();
				for(User peer : randomTargetPeers){
					peer.handleProposedBlock(proposedBlock);
				}
			}
		}
		else {
			appendToLogs("user "+name + " ignored block since computed hash and block's hash are different");
		}
	}

	@Override
	public boolean equals(Object obj) {
		return ((User) obj).name.equals(name);
	}

	public void appendBlock(Block block) {
		ledger.appendBlock(block);
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
