package projet;

public class ElectionMessage extends Message {
	private long compNum;
	private long compId;

	public ElectionMessage(long idsrc, long iddest, String tag, Object content, int pid, long compNum, long compId) {
		super(idsrc, iddest, tag, content, pid);
		this.compNum = compNum;
		this.compId = compId;
	}

	public long getCompNum() {
		return compNum;
	}

	public void setCompNum(int compNum) {
		this.compNum = compNum;
	}

	public long getCompId() {
		return compId;
	}

	public void setCompId(long compId) {
		this.compId = compId;
	}
	
	public String toString()
	{
		return "Election [#" + getIdSrc() + "] -> [#"+ getIdDest() + "] with computation id <" + compNum + "," + compId + ">";
	}
	
}
