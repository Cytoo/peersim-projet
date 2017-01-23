package projet;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class EmitterImpl implements Emitter {

	private static final String PAR_ELECTION = "electionprotocol";
	private static final String PAR_LATENCY = "latency";
	private static final String PAR_SCOPE = "scope";
	private static final String PAR_POSITION = "positionprotocol";

	private final int election_protocol_id;
	private final int position_pid;
	
	private final int latency;
	private final int scope;

	public EmitterImpl(String prefix) {
		election_protocol_id = Configuration.getPid(prefix + "." + PAR_ELECTION);
		latency = Configuration.getInt(prefix + "." + PAR_LATENCY);
		scope = Configuration.getInt(prefix + "." + PAR_SCOPE);
		position_pid = Configuration.getPid(prefix + "." + PAR_POSITION);
	}

	@Override
	public Object clone() {
		EmitterImpl em = null;
		try {
			em = (EmitterImpl) super.clone();
		} catch (CloneNotSupportedException e) {
		} // never happens
		return em;
	}

	@Override
	public void emit(Node host, Message msg) {
		for (int i = 0 ; i < Network.size() ; i++) {
			Node n = Network.get(i);
			
			if (n != host) {
				// Vérifier que n est dans nos voisins
				PositionProtocol posNode = (PositionProtocol) n.getProtocol(position_pid);
				PositionProtocol posHost = (PositionProtocol) host.getProtocol(position_pid);
				double distance = Math.sqrt((posNode.getX() - posHost.getX()) * ((posNode.getX() - posHost.getX())) 
											+(posNode.getY() - posHost.getY()) * ((posNode.getY() - posHost.getY())));
				
				if (distance < this.scope) {
					EDSimulator.add(this.getLatency(), msg, Network.get(i), election_protocol_id);
				}
					
			}
		}
	}

	@Override
	public int getLatency() {
		return latency;
	}

	@Override
	public int getScope() {
		return scope;
	}

}
