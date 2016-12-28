package projet;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class ElectionProtocolImpl implements ElectionProtocol {
	private static final String PAR_EMITTERID = "emitter";
	private static final String PAR_TIMEOUT = "timeout";
	
	private final int protocol_id;
	private final int emitter_id;
	private final int timeout;
	
	private List<Long> neighbors = new ArrayList<Long>(); 
	private List<Long> alive     = new ArrayList<Long>();

	
	
	public ElectionProtocolImpl(String prefix) {
		String tmp[] = prefix.split("\\.");
		protocol_id = Configuration.lookupPid(tmp[tmp.length - 1]);
		emitter_id = Configuration.getPid(prefix + "." + PAR_EMITTERID);
		timeout = Configuration.getInt(prefix + "." + PAR_TIMEOUT);
	}

	@Override
	public Object clone() {
		ElectionProtocolImpl ep = null;
		try {
			ep = (ElectionProtocolImpl) super.clone();
			ep.neighbors = new ArrayList<Long>();
			ep.alive	 = new ArrayList<Long>();
			
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getIDLeader() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMyValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Long> getNeighbors() {
		return neighbors;
	}
}
