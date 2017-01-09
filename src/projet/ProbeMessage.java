package projet;

public class ProbeMessage extends Message {
	private int seqnum;
	
	public ProbeMessage(long idsrc, long iddest, String tag, Object content, int pid, int seqnum) {
		super(idsrc, iddest, tag, content, pid);
		this.seqnum = seqnum;
	}
	
	public int getSeqnum() {
		return seqnum;
	}



	public void setSeqnum(int seqnum) {
		this.seqnum = seqnum;
	}



	public String toString()
	{
		return "ProbeMessage [#" + getIdSrc() + "] -> [#"+ (getIdDest() == -2 ? "ALL" : getIdSrc()) + "] " + 
					((getContent() != null) ? "From leader {" + (Long) getContent() + "} with seqnum °" + seqnum +"."
									: "");
	}
}
