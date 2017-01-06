package projet;

public class AckMessage extends Message {
	private int value;
	private long idMaxValue;
	
	public AckMessage(long idsrc, long iddest, String tag, Object content, int pid, int value, long idMaxValue) {
		super(idsrc, iddest, tag, content, pid);
		this.value = value;
		this.idMaxValue = idMaxValue;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public long getIdMaxValue() {
		return idMaxValue;
	}

	public void setIdMaxValue(long idMaxValue) {
		this.idMaxValue = idMaxValue;
	}
	
	public String toString()
	{
		return "Ack [#" + getIdSrc() + "] -> [#"+ getIdDest() + "] with maxvalue {" + value + "} on [#" + idMaxValue + "]";
	}

}
