import java.util.ArrayList;

public class Ledger {
	private static long blockSize = 5;
	private ArrayList<Block> blocks;

	public Ledger(int n){
		blocks=new ArrayList<Block>();
		blockSize = n;
	}
	
	public Ledger(){
		blocks=new ArrayList<Block>();
		blockSize = 5;
	}
	
	public void appendBlock(Block block){
		blocks.add(block);
		Block lastBlock = blocks.get(blocks.size() - 1);
		block.linkPrevBlock(lastBlock);
	}
	
	public boolean containsNonce(String nonce){
		for(Block block:blocks){
			if(block.getNonce().equals(nonce)){
				return true;
			}
		}
		return false;
	}
	
	public Block lastBlock(){
		return blocks.get(blocks.size() - 1);
	}
	// @Abo el se3od >>>>>>>>>>>>>> Implement the following method
	// It determines whether a proposed block can be linked to the last block of this ledger based on its nonce
	public boolean canBeAppended(ProposedBlock proposedBlock){
		return false;
	}
	
	public static long getBlocksize() {
		return blockSize;
	}
	
	public  ArrayList<Block> getBlocks() {
		return blocks;
	}
}
