import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

public class Main {
	static Hashtable<String, ArrayList<User>> networkGraph;
	static ArrayList<User> usersList;
	static int blockSize;
	static long currentTransactionId;
	
	public static int randomInt(int min, int max){
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	
	// To Send a random number of transactions to some random peers
	// Generate users and select their peers and put them in the Hashtable networkGraph
	
	public static void sendTransactions() throws Exception {
		int setSize = networkGraph.keySet().size();
		int senders = randomInt(0, setSize);
		for (int i = 0; i < senders; i++) {
			int randInt = randomInt(0, setSize-1);
			User user = usersList.get(randInt);
			int tranRand = randomInt(0, 10);
			for (int j = 0; j < tranRand; j++) {
				Transaction trans = user.generateTransaction();
				user.announceTransaction(trans);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		networkGraph = new Hashtable<String,ArrayList<User>>();
		blockSize = 5;
		currentTransactionId = 1;
		usersList = new ArrayList<>();
		
		User a = new User("A");
		User b = new User("B");
		User c = new User("C");
		User d = new User("D");
		User e = new User("E");
		
		usersList.add(a);
		usersList.add(b);
		usersList.add(c);
		usersList.add(d);
		usersList.add(e);
		
		ArrayList<User> aPeers = new ArrayList<>();
		aPeers.add(b);
		aPeers.add(c);
		aPeers.add(d);
		aPeers.add(e);
		
		ArrayList<User> bPeers = new ArrayList<>();
		bPeers.add(c);
		bPeers.add(a);
		bPeers.add(d);
		
		ArrayList<User> cPeers = new ArrayList<>();
		cPeers.add(b);
		cPeers.add(a);
		cPeers.add(e);
		
		ArrayList<User> dPeers = new ArrayList<>();
		dPeers.add(b);
		dPeers.add(a);
		dPeers.add(e);
		
		ArrayList<User> ePeers = new ArrayList<>();
		ePeers.add(c);
		ePeers.add(a);
		ePeers.add(d);
		
		networkGraph.put(a.getName(), aPeers);
		networkGraph.put(b.getName(), bPeers);
		networkGraph.put(c.getName(), cPeers);
		networkGraph.put(d.getName(), dPeers);
		networkGraph.put(e.getName(), ePeers);
		
		sendTransactions();

		//If you want to test signature
//		a.announceTransaction(a.generateTransaction());
//		System.out.println(a.getTransactions());
//		Transaction t=a.getTransactions().get(0);
//		System.out.println(a.verifySignature(t.getSignature(),t.getContent(),a.getPublicKey()));
	}
}
