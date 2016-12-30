package projet;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class ElectionProtocolImpl implements ElectionProtocol {
	private static final String PAR_EMITTERID = "emitter";
	private static final String PAR_TIMEOUT = "timeout";
	private static final String PAR_MAXVALUE = "maxvalue";
	
	private final int protocol_id;
	private final int emitter_id;
	private final int timeout;
	private final int maxvalue;
	
	private List<Long> neighbors = new ArrayList<Long>(); 
	private List<Long> alive     = new ArrayList<Long>();
	private int myValue;
	private boolean inElection;
	private int idLeader;
	private int numSeq = 0;
	
	
	public ElectionProtocolImpl(String prefix) {
		String tmp[] = prefix.split("\\.");
		protocol_id = Configuration.lookupPid(tmp[tmp.length - 1]);
		emitter_id = Configuration.getPid(prefix + "." + PAR_EMITTERID);
		timeout = Configuration.getInt(prefix + "." + PAR_TIMEOUT);
		maxvalue = Configuration.getInt(prefix + "." + PAR_MAXVALUE);
		
		myValue = CommonState.r.nextInt(maxvalue);
		inElection = true;
	}

	private void newElection(Node node, int pid) {
		//inElection = true;

		Emitter em = (Emitter) node.getProtocol(emitter_id);
		em.emit(node, new ElectionMessage(node.getID(), Emitter.ALL, "election", null, protocol_id, numSeq, node.getID()));
		numSeq++;
		
	}

	@Override
	public Object clone() {
		ElectionProtocolImpl ep = null;
		try {
			ep = (ElectionProtocolImpl) super.clone();
			ep.neighbors = new ArrayList<Long>();
			ep.alive	 = new ArrayList<Long>();
			ep.myValue = CommonState.r.nextInt(maxvalue);
			ep.numSeq = 0;
			ep.inElection = true;
	
			
		} catch (CloneNotSupportedException e) {
		} // never happens
		return ep;
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		if (protocol_id != pid) {
			throw new RuntimeException("Receive Message for wrong protocol");
		}

		if (event instanceof ProbeMessage)
		{
			ProbeMessage msg = (ProbeMessage) event;
			
			if(!getNeighbors().contains(msg.getIdSrc()))
			{
				getNeighbors().add(msg.getIdSrc());
			}
			if(!alive.contains(msg.getIdSrc()))
			{
				alive.add(msg.getIdSrc());
			}
			
		}
		else if (event instanceof ElectionMessage) {			
			ElectionMessage msg = (ElectionMessage) event;

			
		}
		
		else if (event instanceof Message) {			
			Message msg = (Message) event;

			// If node is the receiver OR it is a broadcast
			if (msg.getIdDest() == node.getID() || msg.getIdDest() == Emitter.ALL) {
				//Then the message is delivered to the application
				//TODO
			}
		}
		else if(event == null)
		{
			ListIterator<Long> it = getNeighbors().listIterator(); 
			while(it.hasNext())
			{
				Long id = it.next();
				if(!alive.contains(id))
				{
					it.remove();
				}
			}
			alive.clear();
			
			Emitter em = (Emitter) node.getProtocol(emitter_id);
			em.emit(node, new ProbeMessage(node.getID(), Emitter.ALL, "probe", null, protocol_id));
			
			EDSimulator.add(timeout, null, node, pid);
		}
		
	}

	@Override
	public boolean isInElection() {
		return inElection;
	}

	@Override
	public long getIDLeader() {
		return idLeader;
	}

	@Override
	public int getMyValue() {
		// TODO Auto-generated method stub
		return myValue;
	}

	@Override
	public List<Long> getNeighbors() {
		return neighbors;
	}
}
