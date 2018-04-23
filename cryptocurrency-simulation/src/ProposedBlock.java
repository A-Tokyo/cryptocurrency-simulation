import java.util.ArrayList;

public class ProposedBlock extends Block{
	User proposer;
	ArrayList<String> uniqueVoters;
	ArrayList<User> rejectedVote;
	int confirmations, rejections;

	public ProposedBlock(ArrayList<Transaction> transactions, String nonce, User proposer) {
		super(transactions, nonce);
		this.proposer = proposer;
		this.uniqueVoters = new ArrayList<>();
		this.confirmations = 0;
		this.rejections = 0;
	}
}
