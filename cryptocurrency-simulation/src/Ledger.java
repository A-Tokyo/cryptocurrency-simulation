import java.util.ArrayList;

public class Ledger {
	private static long blockSize = 5; //number of transactions to form block
	private ArrayList<Block> blocks;
	
	public Ledger(int n){
		blocks=new ArrayList<Block>();
		blockSize = 5;
	}
	
	public Ledger(){
		blocks=new ArrayList<Block>();
		blockSize = 5;
	}
	
	public void appendBlock(Block block){
		blocks.add(block);
	}
	
	public boolean containsNonce(String nonce){
		for(Block block:blocks){
			if(block.getNonce().equals(nonce)){
				return true;
			}
		}
		return false;
	}
	
	public static long getBlocksize() {
		return blockSize;
	}
	
	public  ArrayList<Block> getBlocks() {
		return blocks;
	}
}
