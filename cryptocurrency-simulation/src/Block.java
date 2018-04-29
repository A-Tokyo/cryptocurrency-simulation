import java.util.ArrayList;

public class Block {
	private String nonce;
	private ArrayList<Transaction> transactions;
	private Block prevBlock;
	private String prevHash;
	private String hash;
	
	public Block(ArrayList<Transaction> transactions,String nonce,String hash){
		this.transactions = transactions;
		this.nonce=nonce;
		this.hash=hash;
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
	
	public String getPrevHash(){
		return prevHash;
	}
	public String getHash(){
		return hash;
	}
	
	public Block getCopy(){
		return new Block(transactions, nonce,hash);
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		Block block=(Block) obj;
		
		return this.hash==block.hash;
	}

	public void linkPrevBlock(Block prev){
		this.prevBlock = prev;
		this.prevHash=prev.getHash();
	}
}

