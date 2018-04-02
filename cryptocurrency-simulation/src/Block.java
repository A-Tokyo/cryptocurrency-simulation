import java.util.ArrayList;

public class Block {
	private String nonce;
	private ArrayList<Transaction> transactions;
	
	public Block(ArrayList<Transaction> transactions,String nonce){
		this.transactions = transactions;
		this.nonce=nonce;
	}
	
	public ArrayList<Transaction> getTransactions(){
		return transactions;
	}
	public String getNonce(){
		return nonce;
	}
}

