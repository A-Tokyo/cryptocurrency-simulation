import java.util.ArrayList;
import java.util.Hashtable;

public class Main {
	static Hashtable<String, ArrayList<User>> networkGraph;
	static int blockSize;
	static long currentTransactionId;

	public static void main(String[] args) {
		networkGraph = new Hashtable<>();
		blockSize = 5;
		currentTransactionId = 1;
		
		User a = new User("A");
		User b = new User("B");
		User c = new User("C");
		User d = new User("D");
		User e = new User("E");
		
		ArrayList<User> aPeers = new ArrayList<>();
		aPeers.add(new User("B"));
		aPeers.add(new User("C"));
		aPeers.add(new User("D"));
		aPeers.add(new User("E"));
		
		ArrayList<User> bPeers = new ArrayList<>();
		bPeers.add(new User("C"));
		bPeers.add(new User("A"));
		bPeers.add(new User("D"));
		
		ArrayList<User> cPeers = new ArrayList<>();
		cPeers.add(new User("B"));
		cPeers.add(new User("A"));
		cPeers.add(new User("E"));
		
		ArrayList<User> dPeers = new ArrayList<>();
		dPeers.add(new User("B"));
		dPeers.add(new User("A"));
		dPeers.add(new User("E"));
		
		ArrayList<User> ePeers = new ArrayList<>();
		ePeers.add(new User("C"));
		ePeers.add(new User("A"));
		ePeers.add(new User("D"));
		
		networkGraph.put(a.getName(), aPeers);
		networkGraph.put(b.getName(), bPeers);
		networkGraph.put(c.getName(), cPeers);
		networkGraph.put(d.getName(), dPeers);
		networkGraph.put(e.getName(), ePeers);
		
		a.announceTransaction(a.generateTransaction());
	}
}
