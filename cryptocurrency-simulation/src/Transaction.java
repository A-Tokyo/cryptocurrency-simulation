
public class Transaction {
	private long id;
	private User announcer;
	private String content;
	
	public Transaction(long id, User announcer, String content){
		this.id = id;
		this.announcer = announcer;
		this.content = content;
	}
	
	@Override
	public String toString() {
		return "ID: " + id + ", Announcer: " + announcer + ", Content: " + content;
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

	public User getAnnouncer() {
		return announcer;
	}

	public void setAnnouncer(User announcer) {
		this.announcer = announcer;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
