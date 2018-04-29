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
	ArrayList<ArrayList<Block>> cache;

	public Ledger getLedger() {
		return ledger;
	}

	public User(String name){
		this.name = name;
		this.transactions = new ArrayList<Transaction>();
		this.ledger = new Ledger();
		this.cache = new ArrayList<>();
		generateKeys();
		initCache(5);

		//uncomment this segment to have users with public key modulus and exponent as name //TODO: uncomment
		//		RSAPublicKey rspk=(RSAPublicKey)  publicKey;
		//		this.name=rspk.getModulus().toString()+rspk.getPublicExponent().toString();
	}

	public void appendToLogs(String text) throws IOException{
		text = "\n" + text + "\n";
		Files.write(Paths.get("logs.txt"), text.getBytes(), StandardOpenOption.APPEND);
	}

	public void initCache(int size){
		for (int i = 0; i < size; i++) {
			cache.add(new ArrayList<>());
		}
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

	public Boolean authenticateBlockTransactions(Block block){
		for(Transaction transaction : block.getTransactions()){
			PublicKey pk = getOriginatorPublicKey(transaction);
			try {
				if(!verifySignature(transaction.getSignature(),transaction.getContent(),pk)) //if verification false
					return false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	//verify that the hashing of block is consistent to what the block contains
	public Boolean verifyBlockHash(Block block){
		System.out.print(".");
		String hashStr=block.getTransactions().toString()+block.getPrevBlock().getHash()+block.getNonce();
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
			String hash = generateHash(nonce);
			Block block = new Block(transactions,nonce,hash);
			block.linkPrevBlock(ledger.lastBlock());
			appendBlock(block);
			announceBlock(block);
		} 
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createBlock(ArrayList<Transaction> trans) throws IOException{
		try {
			boolean isContainsNonce=false;
			String nonce;
			do{
				nonce=generateNonce();
				isContainsNonce=ledger.containsNonce(nonce);
			}
			// Make sure the nonce starts with 00 and not in ledger before (puzzle)
			while(isContainsNonce || !nonce.startsWith("00"));
			String hash = generateHash(nonce);
			Block block = new Block(trans,nonce,hash);

			block.linkPrevBlock(ledger.lastBlock());
			appendBlock(block);
			announceBlock(block);
		} 
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void announceBlock(Block block) throws IOException{
		// If managed to solve a complex puzzle, a user can then announce a block
		ProposedBlock proposedBlock = new ProposedBlock(block,this);
		ArrayList<User> randomTargetPeers = selectTargetPeers();
		appendToLogs(name + " : Created a block and will Announce to " +randomTargetPeers);
		for(User peer : randomTargetPeers){
			peer.handleProposedBlock(proposedBlock);
		}
	}

	public void checkCache(ProposedBlock proposedBlock){
		for(ArrayList<Block> current_cache : cache){
			if(current_cache.isEmpty()){
				current_cache.addAll(ledger.getBlocks());
			}

			// Add the proposed block to the current chain if it's possible
			Block current_last = current_cache.get(current_cache.size()-1);
			if(proposedBlock.getPrevBlock().equals(current_last)){
				current_cache.add(proposedBlock);
			}

			// If the current cache is larger than the ledger, swap them
			if(current_cache.size() > ledger.getBlocks().size()){
				ArrayList<Block> temp = new ArrayList<>();
				temp.addAll(ledger.getBlocks());

				ledger.getBlocks().clear();
				ledger.getBlocks().addAll(current_cache);
				current_cache.addAll(temp);
			}

			// Drop the current cache if the ledger is larger with 3 or more blocks
			if(ledger.getBlocks().size() - current_cache.size() >= 3){
				cache.remove(current_cache);
			}
		}
	}

	public void updateLedgerAndCache(ProposedBlock proposedBlock){
		if(ledger.getBlocks().size() >= 1){
			Block last = ledger.getBlocks().get(ledger.getBlocks().size()-1);

			if(proposedBlock.getPrevBlock().equals(last)){
				appendBlock(proposedBlock);
			}
		}

		if(ledger.getBlocks().size() >= 2){
			Block before_last = ledger.getBlocks().get(ledger.getBlocks().size()-2);

			if(proposedBlock.getPrevBlock().equals(before_last)){
				checkCache(proposedBlock);
			}
		}

		if(ledger.getBlocks().size() >= 3){
			Block before_before_last = ledger.getBlocks().get(ledger.getBlocks().size()-3);

			if(proposedBlock.getPrevBlock().equals(before_before_last)){
				checkCache(proposedBlock);
			}
		}
	}

	public void handleProposedBlock(ProposedBlock proposedBlock) throws IOException{
		String proposerName = proposedBlock.proposer.getName();
		appendToLogs(name + " : Received a block from "+ proposerName + ". Verifying it first.");

		// Verify that the hashing of block is consistent with the contents of the block first, if not it will be ignored
		if(verifyBlockHash(proposedBlock)){
			appendToLogs(name + " : Sucessfully verified the recieved block");

			if(authenticateBlockTransactions(proposedBlock)){ //authenticate transactions in block
				appendToLogs(name + " : Sucessfully Authenticated the transactions in the recieved Block");
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
				if(proposedBlock.uniqueVoters.size() == Main.usersCount - 1){
					// All users -except the proposer- voted for the block
					if(proposedBlock.confirmations > proposedBlock.rejections){
						appendToLogs(proposerName + " : My block is accepted");
						proposedBlock.proposer.transactions.clear();

						// Update all users to include the accepted block
						Main.mainLedger.appendBlock(proposedBlock);
						Main.updateUsersLedgers();
					}
					else{
						appendToLogs(proposerName + " : My block is orphaned. Trying again.");
						proposedBlock.proposer.createBlock(proposedBlock.getTransactions());
					}
				}
				else{
					if(ledger.canBeAppended(proposedBlock)){
						proposedBlock.confirmations++;
						updateLedgerAndCache(proposedBlock);
					}
					else{
						proposedBlock.rejections++;
					}

					// After voting, pass the block to a random set of near peers
					ArrayList<User> randomTargetPeers = selectTargetPeers();
					for(User peer : randomTargetPeers){
						peer.handleProposedBlock(proposedBlock);
					}
				}
			}
			else{
				appendToLogs("Authentication of Transactions in Block failed due to invalid signatures therfore willl ignore the block");
			}
		}
		else {
			appendToLogs(name + " : Ignored block since computed hash and block's hash are different");
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
