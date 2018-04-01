import java.util.ArrayList;

public class Ledger {
	private static long blockSize;
	private static long currentNonce;
	private static ArrayList<Block> blocks;
	
	public Ledger(){
		blockSize = 5;
		currentNonce = 0;
	}
	
	public static void appendBlock(Block block){
		blocks.add(block);
		currentNonce++;
	}

	public static long getBlocksize() {
		return blockSize;
	}
	
	public static ArrayList<Block> getBlocks() {
		return blocks;
	}
	
	public static long getCurrentNonce() {
		return currentNonce;
	}
}
