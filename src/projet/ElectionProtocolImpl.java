package projet;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class ElectionProtocolImpl implements ElectionProtocol, MetricsProtocol {
	private static final String PAR_EMITTERID = "emitter";
	private static final String PAR_TIMEOUT = "timeout";
	private static final String PAR_MAXVALUE = "maxvalue";
	private static final String PAR_BEACONINTERVAL = "beaconinterval";
	private static final String PAR_MAXBEACONLOSS = "maxbeaconloss";

	private final int protocol_id;
	private final int emitter_id;
	private final int timeout;
	private final int maxvalue;
	private final int maxbeaconloss;
	private final int beaconinterval;

	private List<Long> neighbors = new ArrayList<Long>();
	private List<Long> alive = new ArrayList<Long>();
	private List<Long> pending = new ArrayList<Long>(); // set of nodes from
														// which we hear an ack
														// from
	private int myValue;

	private boolean inElection; // a binary variable indicating if is currently
								// in an election or not
	private boolean ack = false; // a binary variable indicating if has sent ack
									// to parent or not

	private long parent; // parent node in the spanning tree
	// Computation related variables
	private long compId; // computation-idex id
	private long compNum; // computation-idex num
	private int numSeq = 0;
	// Leader Identity and value variables
	private int leaderValue; // Maximum downstream value
	private boolean leaderAlive; // Heard from leader recently
	private long idLeader; // leader
	// Leader Heartbeat related variables
	private int heartbeatSeq = 1; // Our own heartbeat seqnum
	private int lastHeartbeatSeq = 0; // last heartbeat seqnum received from
										// leader
	private int numberbeaconLoss = 0; // Number of time we lost the beacon
	private LeaderMessage leaderBuffer = null; // Buffer of leadermessage
	
	
	/*
	 *	Metrics variables 
	 * 
	 * 	 */
	private  int timeNoLeader;			//Total time spent in election
	private  int numberOfElections;		//Number of elections
	private	 int numberOfMessages;		//number of messages sent
	private  int timeStart;				//time at which we start logging statistics
	private  int timeBeginElection;		//time of last election start

	public ElectionProtocolImpl(String prefix) {
		String tmp[] = prefix.split("\\.");
		protocol_id = Configuration.lookupPid(tmp[tmp.length - 1]);
		emitter_id = Configuration.getPid(prefix + "." + PAR_EMITTERID);
		timeout = Configuration.getInt(prefix + "." + PAR_TIMEOUT);
		maxvalue = Configuration.getInt(prefix + "." + PAR_MAXVALUE);
		maxbeaconloss = Configuration.getInt(prefix + "." + PAR_MAXBEACONLOSS);
		beaconinterval = Configuration.getInt(prefix + "." + PAR_BEACONINTERVAL);

		myValue = CommonState.r.nextInt(maxvalue);
		inElection = true;
		timeBeginElection = CommonState.getIntTime();
		
		parent = -1;
		leaderValue = myValue;
	}

	public void newElection(Node node, int pid) {
		pending = new ArrayList<Long>();
		compId = node.getID();
		compNum = numSeq;

		ack = false;
		leaderValue = myValue;
		idLeader = node.getID();
		numberOfElections++;
		Emitter em = (Emitter) node.getProtocol(emitter_id);

		if (neighbors.isEmpty()) {
			// If we start an election and we are with no neighbors then we are
			// our own connex graph
			inElection = false;
			leaderAlive = true;
			return;
		}

		for (Long n : neighbors) {
			em.emit(node, new ElectionMessage(node.getID(), n, "election", null, protocol_id, compNum, compId));
			numberOfMessages++;
			
			pending.add(n);
		}
		numSeq++;
		inElection = true;
		timeBeginElection = CommonState.getIntTime();
		
		
		parent = -1;
	}

	@Override
	public Object clone() {
		ElectionProtocolImpl ep = null;
		try {
			ep = (ElectionProtocolImpl) super.clone();
			ep.neighbors = new ArrayList<Long>();
			ep.alive = new ArrayList<Long>();
			ep.myValue = CommonState.r.nextInt(maxvalue);
			ep.numSeq = 0;
			ep.inElection = true;
			ep.timeBeginElection = CommonState.getIntTime();
			
			ep.parent = -1;
			ep.leaderValue = ep.myValue;
			ep.pending = new ArrayList<Long>();
			ep.lastHeartbeatSeq = 0;
			ep.heartbeatSeq = 1;
			ep.numberbeaconLoss = 0;

		} catch (CloneNotSupportedException e) {
		} // never happens
		return ep;
	}

	private void processProbe(Node node, int pid, ProbeMessage msg) {

		// If we get a probemessageFrom leader
		if (msg.getContent() != null && !inElection && (Long) msg.getContent() == idLeader) {
			if (msg.getSeqnum() > lastHeartbeatSeq) {
				lastHeartbeatSeq = msg.getSeqnum();
				leaderAlive = true;
				numberbeaconLoss = 0;

				Emitter em = (Emitter) node.getProtocol(emitter_id);
				em.emit(node, new ProbeMessage(node.getID(), Emitter.ALL, "probe", msg.getContent(), protocol_id,
						msg.getSeqnum()));
				return;
			}
		} else if (msg.getContent() != null) {
			return;
		}

		if (!getNeighbors().contains(msg.getIdSrc())) {
			getNeighbors().add(msg.getIdSrc());

			if (!inElection) {
				Emitter em = (Emitter) node.getProtocol(emitter_id);
				em.emit(node, new LeaderMessage(node.getID(), msg.getIdSrc(), "leader", null, protocol_id, leaderValue,
						idLeader));
				numberOfMessages++;
			}

		}
		if (!alive.contains(msg.getIdSrc())) {
			alive.add(msg.getIdSrc());
		}
	}

	private void processElection(Node node, int pid, ElectionMessage msg) {
		// if init
		if (msg.getContent() != null) {
			newElection(node, pid);
			return;
		}

		// If we have a leader
		if (!inElection) {
			inElection = true;
			timeBeginElection = CommonState.getIntTime();
			numberOfElections++;
			
			compId = msg.getCompId();
			compNum = msg.getCompNum();
			parent = msg.getIdSrc();
			pending = new ArrayList<Long>();
			ack = false;
			leaderValue = myValue;
			idLeader = node.getID();
			lastHeartbeatSeq = 0;
		} else {
			// If we are already in election and we receive election message
			// from someone who is not
			// our parent
			if (msg.getCompNum() == compNum && msg.getCompId() == compId) {
				Emitter em = (Emitter) node.getProtocol(emitter_id);
				em.emit(node, new AckMessage(node.getID(), msg.getIdSrc(), "ack", null, protocol_id, -1, node.getID()));
				numberOfMessages++;
				return;
			}
			// Compare computation-index
			if ((msg.getCompNum() > compNum) || ((msg.getCompNum() == compNum) && (msg.getCompId() > compId))) {
				numberOfElections++;
				
				
				compNum = msg.getCompNum();
				compId = msg.getCompId();
				parent = msg.getIdSrc();
				pending = new ArrayList<Long>();
				ack = false;
				leaderValue = myValue;
				idLeader = node.getID();
				lastHeartbeatSeq = 0;
			} else {
				return; // Election sent does not win over the current.
						// Discarded.
			}

		}
		// We forward the election message to all neighbors except parent
		for (Long n : neighbors) {
			if (n != parent) {
				Emitter em = (Emitter) node.getProtocol(emitter_id);
				em.emit(node, new ElectionMessage(node.getID(), n, "election", null, protocol_id, compNum, compId));
				numberOfMessages++;
				
				pending.add(n);
			}
		}
		if (pending.isEmpty()) {
			Emitter em = (Emitter) node.getProtocol(emitter_id);
			em.emit(node, new AckMessage(node.getID(), parent, "ack", null, protocol_id, myValue, node.getID()));
			numberOfMessages++;
			ack = true;
		}

	}

	private void processLeader(Node node, int pid, LeaderMessage msg) {

		if (msg.getIdSrc() == node.getID()) {
			return;
		}

		if (inElection) {
			if (!ack) {
				if (leaderBuffer == null || msg.getLeaderValue() > leaderBuffer.getLeaderValue()) {
					leaderBuffer = msg;
				}

				return;
			}

			if (leaderBuffer != null && leaderBuffer.getLeaderValue() > msg.getLeaderValue()) {
				msg = leaderBuffer;
				leaderBuffer = null;
			}

			idLeader = msg.getIdLeader();
			leaderValue = msg.getLeaderValue();

			Emitter em = (Emitter) node.getProtocol(emitter_id);
			em.emit(node,
					new LeaderMessage(node.getID(), Emitter.ALL, "leader", null, protocol_id, leaderValue, idLeader));
			numberOfMessages++;

			inElection = false;
			timeNoLeader += CommonState.getIntTime() - timeBeginElection;
			
			leaderAlive = true;
			lastHeartbeatSeq = 0;
			numberbeaconLoss = 0;
		} else {
			// Receiving a leader message when we are not in Election
			// Comparison between merging component leader and our leader
			if (msg.getLeaderValue() > leaderValue) {
				leaderValue = msg.getLeaderValue();
				idLeader = msg.getIdLeader();
				lastHeartbeatSeq = 0;
				numberbeaconLoss = 0;

				Emitter em = (Emitter) node.getProtocol(emitter_id);
				em.emit(node, new LeaderMessage(node.getID(), Emitter.ALL, "leader", null, protocol_id, leaderValue,
						idLeader));
				numberOfMessages++;
			}
		}
	}

	private void processDelta(Node node, int pid) {
		ListIterator<Long> it = getNeighbors().listIterator();
		while (it.hasNext()) {
			Long id = it.next();
			if (!alive.contains(id)) {
				it.remove();

				// If we received all the acks we expected
				if (pending.remove(id) && pending.isEmpty()) {
					onPendingEmpty(node, pid);
				}

				if (inElection && id == parent) {
					parent = -1;
				}
			}
		}
		if(neighbors.isEmpty() && inElection)
		{
			newElection(node, pid);
		}
		alive.clear();

		Emitter em = (Emitter) node.getProtocol(emitter_id);
		em.emit(node, new ProbeMessage(node.getID(), Emitter.ALL, "probe", null, protocol_id, 0));

		EDSimulator.add(timeout, null, node, pid);
	}

	private void processBeacon(Node node, int pid) {
		if (!inElection) {

			if (idLeader != node.getID()) {
				// If we have a leader we need to hear from it every so often
				if (leaderAlive == true) {
					// Yipee yay we did
					leaderAlive = false;
					numberbeaconLoss = 0;
				} else if (numberbeaconLoss < maxbeaconloss -1 ) {
					// We did not hear from it every so often
					// Need to trigger a new election
					numberbeaconLoss++;
				} else {
					newElection(node, pid);
				}
			}

			if (!inElection && idLeader == node.getID()) {
				Long probeleader = null;
				// If we have a leader and WE are the leader
				// we broadcast a probemessage indicating ourselves as the
				// leader
				probeleader = new Long(node.getID());

				Emitter em = (Emitter) node.getProtocol(emitter_id);
				em.emit(node,
						new ProbeMessage(node.getID(), Emitter.ALL, "probe", probeleader, protocol_id, heartbeatSeq));
				heartbeatSeq++;
			}

		}

		EDSimulator.add(beaconinterval * 1000, new Integer(0), node, pid);
	}

	private void processAck(Node node, int pid, AckMessage msg) {
		if (msg.getValue() > leaderValue) {
			idLeader = msg.getIdMaxValue();
			leaderValue = msg.getValue();
			lastHeartbeatSeq = 0;
		}

		

		// If we received all the acks we expected
		if (pending.remove(msg.getIdSrc()) && pending.isEmpty()) {
			onPendingEmpty(node, pid);
		}
	}
	private void onPendingEmpty(Node node, int pid)
	{
		// if we are the tree root
		if (parent == -1) {
			// we broadcast our leader
			Emitter em = (Emitter) node.getProtocol(emitter_id);
			em.emit(node, new LeaderMessage(node.getID(), Emitter.ALL, "leader", null, protocol_id, leaderValue,
					idLeader));
			numberOfMessages++;

			// stop participating in an election
			inElection = false;
			timeNoLeader += CommonState.getIntTime() - timeBeginElection;
			leaderAlive = true;
			lastHeartbeatSeq = 0;
		}

		// else if we are not the tree root
		else {
			// forward ack to parent with maxdownstreamvalue
			Emitter em = (Emitter) node.getProtocol(emitter_id);
			em.emit(node, new AckMessage(node.getID(), parent, "ack", null, protocol_id, leaderValue, idLeader));
			numberOfMessages++;
			ack = true;

		}
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		if (protocol_id != pid) {
			throw new RuntimeException("Receive Message for wrong protocol");
		}
		if (event instanceof Message) {
			Message mg = (Message) event;
			if (!(mg.getIdDest() == node.getID() || mg.getIdDest() == Emitter.ALL)) {
				return;
			}
		}

		if (event instanceof ProbeMessage) {
			ProbeMessage msg = (ProbeMessage) event;

			processProbe(node, pid, (ProbeMessage) event);
		} else if (event instanceof ElectionMessage) {

			processElection(node, pid, (ElectionMessage) event);
		} else if (event instanceof AckMessage) {
			processAck(node, pid, (AckMessage) event);
		}

		else if (event instanceof LeaderMessage) {
			processLeader(node, pid, (LeaderMessage) event);
		} 

		/*********
		 * ** ** DELTA UPDATE ** PROBE HEARBEAT
		 */
		else if (event instanceof Integer) {
			processBeacon(node, pid);
		} else if (event == null) {
			processDelta(node, pid);
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

	@Override
	public void reset() {
			numberOfMessages  = 0;
			numberOfElections = 0;
			timeBeginElection = CommonState.getIntTime();
			timeStart		  = CommonState.getIntTime();
			timeNoLeader      = 0;
			
	}

	@Override
	public double fractionWithoutLeader() {
		return (timeNoLeader / (double) (CommonState.getIntTime() - timeStart));
	}

	@Override
	public double meanElectionRate() {
		return ((double) (numberOfElections * 60000) / (double) (CommonState.getIntTime() - timeStart));
	}

	@Override
	public double meanElectionTime() {
		//System.out.println("timeNoLeader / numberOfElections : " + timeNoLeader + "/" + (double) numberOfElections);
		return ((timeNoLeader / (double) numberOfElections));
	}

	@Override
	public double meanMessageOverhead() {
		//System.out.println("numberOfMessages / numberOfElections : " + numberOfMessages + "/" + (double) numberOfElections);
		return (numberOfMessages / (double) numberOfElections);
	}


}
