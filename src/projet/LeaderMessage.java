package projet;

public class LeaderMessage extends Message {

	private int leaderValue;
	private long idLeader;
	
	public int getLeaderValue() {
		return leaderValue;
	}

	public void setLeaderValue(int leaderValue) {
		this.leaderValue = leaderValue;
	}

	public long getIdLeader() {
		return idLeader;
	}

	public void setIdLeader(long idLeader) {
		this.idLeader = idLeader;
	}

	public LeaderMessage(long idsrc, long iddest, String tag, Object content, int pid, int leaderValue, long idLeader) {
		super(idsrc, iddest, tag, content, pid);
		this.leaderValue = leaderValue;
		this.idLeader = idLeader;
	}

}
