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
	private List<Long> pending   = new ArrayList<Long>(); // set of nodes from which we hear an ack from
	private int myValue;
	
	
	private boolean inElection; //a binary variable indicating if is currently in an election or not
	private boolean ack = false; // a binary variable indicating if has sent ack to parent or not
	private long idLeader; // leader
	private int numSeq = 0;
	private long parent; // parent node in the spanning tree
	private long compId; // computation-idex id
	private long compNum; // computation-idex num
	
	public ElectionProtocolImpl(String prefix) {
		String tmp[] = prefix.split("\\.");
		protocol_id = Configuration.lookupPid(tmp[tmp.length - 1]);
		emitter_id = Configuration.getPid(prefix + "." + PAR_EMITTERID);
		timeout = Configuration.getInt(prefix + "." + PAR_TIMEOUT);
		maxvalue = Configuration.getInt(prefix + "." + PAR_MAXVALUE);
		
		myValue = CommonState.r.nextInt(maxvalue);
		inElection = true;
	}

	public void newElection(Node node, int pid) {
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
		else if (event instanceof ElectionMessage)
		{			
			ElectionMessage msg = (ElectionMessage) event;
			// If we have a leader
			if (!inElection) 
			{
				inElection = true;
				compId = msg.getCompId();
				compNum = msg.getCompNum();
				parent = msg.getIdSrc();
				pending = new ArrayList<Long>();
				ack = false;
			}
			else 
			{
				// If we are already in election and we receive election message from someone who is not 
				// our parent
				if (msg.getCompNum() == compNum && msg.getCompId() == compId)
				{
					Emitter em = (Emitter) node.getProtocol(emitter_id);
					em.emit(node, new AckMessage(node.getID(), msg.getIdSrc(), "ack", null, protocol_id, -1, node.getID()));
					return;
				}
				// Compare computation-index
				if ((msg.getCompNum() > compNum) || ((msg.getCompNum() == compNum) && (msg.getCompId() > compId)))
				{
					compNum = msg.getCompNum();
					compId = msg.getCompId();
					parent = msg.getIdSrc();
					pending = new ArrayList<Long>();
					ack = false;
				}
				else
				{
					return; //Election sent does not win over the current. Discarded.
				}
				
			}
			// We forward the election message to all neighbors except parent
			for(Long n : neighbors)
			{
				if(n != parent)
				{
					Emitter em = (Emitter) node.getProtocol(emitter_id);
					em.emit(node, new ElectionMessage(node.getID(), n, "election", null, protocol_id, compNum, compId));
					pending.add(n);
				}
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
		
		/*********
		 * **
		 * ** 	DELTA UPDATE **
		 * PROBE HEARBEAT
		 */
		else if(event == null)
		{
			ListIterator<Long>  it = getNeighbors().listIterator(); 
			while(it.hasNext())
			{
				Long id = it.next();
				if(!alive.contains(id))
				{
					it.remove();
					
					// We don't expect ack from this node anymore
					pending.remove(id);
					
					/* page 6, para 2
					 * TODO TODO TODO
					 * if lose parent
					 * we send Leader cause we can't participate in current election (IF WE ARE IN ELECTION)
					 */
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
		return myValue;
	}

	@Override
	public List<Long> getNeighbors() {
		return neighbors;
	}
	
	
}
