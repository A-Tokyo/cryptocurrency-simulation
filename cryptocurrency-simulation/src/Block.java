import java.util.ArrayList;

public class Block {
	private String nonce;
	private ArrayList<Transaction> transactions;
	private Block prevBlock;
	
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
	
	public Block getPrevBlock(){
		return prevBlock;
	}
	
	public Block getCopy(){
		return new Block(transactions, nonce);
	}

	public void linkPrevBlock(Block prev){
		this.prevBlock = prev.getCopy();
		
		// @Abo el se3od >>>>>>>>>>>>>> Connect the 2 nonces
	}
}

