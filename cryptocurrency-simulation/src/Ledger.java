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
		Block lastBlock =lastBlock();
		if(lastBlock==null)
			lastBlock=block;//if no last block then reference itself
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
		if(blocks.isEmpty())
			return null;// no last block
		return blocks.get(blocks.size() - 1);
	}
	// @Abo el se3od >>>>>>>>>>>>>> Implement the following method
	// It determines whether a proposed block can be linked to the last block of this ledger based on its hash
	public boolean canBeAppended(ProposedBlock proposedBlock){ //TODO
//		String prevHash=proposedBlock.getPrevHash();
		return false;
	}
	
	public static long getBlocksize() {
		return blockSize;
	}
	
	public  ArrayList<Block> getBlocks() {
		return blocks;
	}
}
