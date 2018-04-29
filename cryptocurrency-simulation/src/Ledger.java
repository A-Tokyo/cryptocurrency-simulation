import java.util.ArrayList;

public class Ledger {
	private static long blockSize = 5;
	private ArrayList<Block> blocks;

	public Ledger(int n){
		blocks=new ArrayList<Block>();
		blockSize = n;
		//adding genesis block in ledger
		ArrayList<Transaction> genesisTransactions=new ArrayList<Transaction>();
		for(int i=0;i<5;i++){
		genesisTransactions.add(new Transaction("genesis", "genesis", "genesis", null)); //maybe do a genesis key in future
		}
		Block genesisBlock=new Block(genesisTransactions, "genesis", "genesis");
		blocks.add(genesisBlock);
	}
	
	public Ledger(){
		blocks=new ArrayList<Block>();
		blockSize = 5;
		//adding genesis block in ledger
		ArrayList<Transaction> genesisTransactions=new ArrayList<Transaction>();
		for(int i=0;i<5;i++){
		genesisTransactions.add(new Transaction("genesis", "genesis", "genesis", null)); //maybe do a genesis key in future
		}
		Block genesisBlock=new Block(genesisTransactions, "genesis", "genesis");
		blocks.add(genesisBlock);
	}
	
	public void appendBlock(Block block){
		Block lastBlock =lastBlock();
		blocks.add(block);
//		System.out.println("lastB:"+lastBlock);
//		if(lastBlock==null)
//			lastBlock=block;//if no last block then reference itself ->solved with genesis block
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
//		if(blocks.isEmpty())
//			return null;// no last block  >solved with genesis block
//		System.out.println(blocks.size());
		return blocks.get(blocks.size() - 1);
	}
	// It determines whether a proposed block can be linked to the last block of this ledger based on its hash
	public boolean canBeAppended(ProposedBlock proposedBlock){
		return proposedBlock.getPrevBlock().equalsHash(lastBlock());
	}
	
	public static long getBlocksize() {
		return blockSize;
	}
	
	public  ArrayList<Block> getBlocks() {
		return blocks;
	}
}
