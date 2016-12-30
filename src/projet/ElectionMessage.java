package projet;

public class ElectionMessage extends Message {

	private int numSeq;
	private long idElector;

	public ElectionMessage(long idsrc, long iddest, String tag, Object content, int pid, int numSeq, long idElector) {
		super(idsrc, iddest, tag, content, pid);
		this.numSeq = numSeq;
		this.idElector = idElector;
	}

	public int getNumSeq() {
		return numSeq;
	}

	public void setNumSeq(int numSeq) {
		this.numSeq = numSeq;
	}

	public long getIdElector() {
		return idElector;
	}

	public void setIdElector(long idElector) {
		this.idElector = idElector;
	}

}
