import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Random;

public class User {
	private String name;
	private ArrayList<Transaction> transactions;
	private Ledger ledger;
	public User(String name){
		this.name = name;
		ledger = new Ledger();
		this.transactions = new ArrayList<>();
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

	public void receiveTransaction(Transaction transaction){
		transactions.add(transaction);
		
		if(transactions.size() == Ledger.getBlocksize()){
			createBlock();
			transactions.clear();
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
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Transaction> getTransactions() {
		return transactions;
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException {
		User a= new User("mo");
		System.out.println(generateRandomString(5));
		System.out.println(a.generateNonce());
	}
}
