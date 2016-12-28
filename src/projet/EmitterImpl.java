package projet;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class EmitterImpl implements Emitter {

	private static final String PAR_ELECTION = "electionprotocol";
	private static final String PAR_LATENCY = "latency";
	private static final String PAR_SCOPE = "scope";

	private final int election_protocol_id;
	
	private final int latency;
	private final int scope;

	public EmitterImpl(String prefix) {
		election_protocol_id = Configuration.getPid(prefix + "." + PAR_ELECTION);
		latency = Configuration.getInt(prefix + "." + PAR_LATENCY);
		scope = Configuration.getInt(prefix + "." + PAR_SCOPE);
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
			// Vérifier que n est dans nos voisins
			if (n != host) {
				EDSimulator.add(this.getLatency(), msg, Network.get(i), election_protocol_id);
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
