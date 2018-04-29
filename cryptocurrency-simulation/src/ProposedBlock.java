import java.util.ArrayList;

public class ProposedBlock extends Block{
	User proposer;
	ArrayList<String> uniqueVoters;
	ArrayList<User> rejectedVote;
	int confirmations, rejections;

	public ProposedBlock(Block block,User proposer) {
		super(block.getTransactions(), block.getNonce(), block.getHash(),block.getPrevBlock());
		this.proposer = proposer;
		this.uniqueVoters = new ArrayList<>();
		this.confirmations = 0;
		this.rejections = 0;
	} 
}
