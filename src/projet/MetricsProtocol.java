package projet;

import peersim.edsim.EDProtocol;

public interface MetricsProtocol extends EDProtocol {

	public void reset();
	public double fractionWithoutLeader();
	public double meanElectionRate();

	public double meanElectionTime();

	public double meanMessageOverhead();

		
}
