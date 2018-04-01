import java.util.ArrayList;

public class Ledger {
	private static ArrayList<Block> blocks;

	public static void appendBlock(Block block){
		blocks.add(block);
	}

	public static ArrayList<Block> getBlocks() {
		return blocks;
	}
}
