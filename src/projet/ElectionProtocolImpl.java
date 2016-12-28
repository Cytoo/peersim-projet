package projet;

import java.util.ArrayList;
import java.util.List;

import peersim.config.Configuration;
import peersim.core.Node;

public class ElectionProtocolImpl implements ElectionProtocol {
	private final int protocol_id;

	public ElectionProtocolImpl(String prefix) {
		String tmp[] = prefix.split("\\.");
		protocol_id = Configuration.lookupPid(tmp[tmp.length - 1]);
	}

	@Override
	public Object clone() {
		ElectionProtocolImpl ep = null;
		try {
			ep = (ElectionProtocolImpl) super.clone();
		} catch (CloneNotSupportedException e) {
		} // never happens
		return ep;
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		if (protocol_id != pid) {
			throw new RuntimeException("Receive Message for wrong protocol");
		}

		if (event instanceof Message) {			
			Message msg = (Message) event;

			// If node is the receiver OR it is a broadcast
			if (msg.getIdDest() == node.getID() || msg.getIdDest() == Emitter.ALL) {
				//Then the message is delivered to the application
				//TODO
			}
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
		// TODO Auto-generated method stub
		return new ArrayList<Long>();
	}
}
