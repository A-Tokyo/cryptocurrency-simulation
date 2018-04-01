import java.util.ArrayList;

public class Block {
	private ArrayList<Transaction> transactions;
	
	public Block(ArrayList<Transaction> transactions){
		this.transactions = transactions;
	}

	public ArrayList<Transaction> getTransactions(){
		return transactions;
	}
}