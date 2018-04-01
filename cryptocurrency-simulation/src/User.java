import java.util.ArrayList;
import java.util.Random;

public class User {
	private String name;
	private ArrayList<Transaction> transactions;
	
	public User(String name){
		this.name = name;
		this.transactions = new ArrayList<>();
	}
	
	public int randomInt(int min, int max){
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	
	public ArrayList<User> selectTargetPeers(){
		ArrayList<User> nearPeers = Main.networkGraph.get(name);
		ArrayList<User> targets = new ArrayList<>();
		int targetsCount = randomInt(0, nearPeers.size());
		
		for (int i = 0; i < targetsCount; i++) {
			int targetIndex = randomInt(0, nearPeers.size()-1);
			targets.add(nearPeers.get(targetIndex));
			nearPeers.remove(targetIndex);
		}
		
		return targets;
	}
	
	public void announceTransaction(Transaction transaction){
		ArrayList<User> targets = selectTargetPeers();
		System.out.println(name + " : Announcing transaction " + transaction.getId() + " to " + targets);
		
		for(User current : targets){
			current.receiveTransaction(transaction);
		}
	}
	
	public Transaction generateTransaction(){
		return new Transaction(Main.currentTransactionId++, this.getName(), generateRandomString(20));
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
		if(!transactions.contains(transaction)){
			// Receive and see if a block can be formed
			//System.out.println(name + " : received transaction " + transaction.getId() + " from " + transaction.getAnnouncer());
			transactions.add(transaction);
			
			if(transactions.size() == Main.blockSize){
				Block block = new Block(transactions);
				Ledger.appendBlock(block);
				transactions.clear();
				System.out.println("Created a block and appended it to the ledger");
			}
			
			// Forward to some near peers
			transaction.setAnnouncer(this.getName());
			announceTransaction(transaction);
		}
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
