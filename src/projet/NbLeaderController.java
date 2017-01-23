package projet;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class NbLeaderController implements Control {
	private static final String PAR_ELECTIONPID = "electionprotocol";
	private final int election_pid;
	
	int cpt = 0;
	int l = 0;
	double stddevm = 0;
	
	public NbLeaderController(String prefix) {
		election_pid=Configuration.getPid(prefix + "." + PAR_ELECTIONPID);
	}
	
	
	@Override
	public boolean execute() {
		cpt++;
		int cur = 0;
		for(int i = 0; i < Network.size(); i++)
		{
			ElectionProtocol ep = (ElectionProtocol) Network.get(i).getProtocol(election_pid);
			
			if(ep.getIDLeader() == i)
			{
				cur++;
			}
		}
		stddevm += (cur - (double)l / cpt) * (cur - (double)l / cpt);
		 l+= cur;
		System.out.println("mean number of leader" + (double)l / (cpt));
		System.out.println("number of leader stddev: " + Math.sqrt(stddevm / cpt));
		return false;
	}

}
