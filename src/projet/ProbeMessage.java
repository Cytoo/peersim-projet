package projet;

public class ProbeMessage extends Message {
	public ProbeMessage(long idsrc, long iddest, String tag, Object content, int pid) {
		super(idsrc, iddest, tag, content, pid);
		
		
		
	}
	
	public String toString()
	{
		return "ProbeMessage [#" + getIdSrc() + "] -> [#"+ (getIdDest() == -2 ? "ALL" : getIdSrc()) + "]";
	}
}
