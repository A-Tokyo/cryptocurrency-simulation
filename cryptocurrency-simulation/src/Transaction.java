
public class Transaction {
	private long id;
	private String announcer;
	private String content;
	private String originator;
	private byte[]signature;
	
	public Transaction(long id,String originator, String announcer, String content,byte[]signature){
		this.id = id;
		this.announcer = announcer;
		this.content = content;
		this.signature=signature;
		this.originator=originator;
	}
	
	@Override
	public String toString() {
		return "ID: " + id + ", Announcer: " + announcer +", Originator: " + originator + ", Content: " + content;
	}
	
	@Override
	public boolean equals(Object obj) {
		return id == ((Transaction) obj).getId();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAnnouncer() {
		return announcer;
	}
	public void setAnnouncer(String annouuncer) {
		 this.announcer=announcer;
	}
	public String getContent() {
		return content;
	}

	public byte[] getSignature() {
		return signature;
	}

	public String getOriginator() {
		return originator;
	}
}
