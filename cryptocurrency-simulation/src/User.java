import java.util.ArrayList;
import java.util.Random;

public class User {
	private String name;
	private ArrayList<Transaction> transactions;
	
	public User(String name){
		this.name = name;
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
			Block block = new Block(transactions);
			Ledger.appendBlock(block);
			transactions.clear();
		}
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
	
	public static void main(String[] args) {
		System.out.println(generateRandomString(5));
	}
}
