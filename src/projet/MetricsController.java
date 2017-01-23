package projet;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class MetricsController implements Control {
	private static final String PAR_METRICSPID = "metricsprotocol";
	
	private final int metrics_pid;


	
	
	
	boolean initialized = false;
	int cpt = 0;
	
	public MetricsController(String prefix)
	{
		metrics_pid=Configuration.getPid(prefix+"."+PAR_METRICSPID);
	}
	
	private void init()
	{
		//reset protocols
		for(int i = 0; i < Network.size(); i++)
		{
			Node n = Network.get(i);
			
			MetricsProtocol metrics = (MetricsProtocol) n.getProtocol(metrics_pid);
			metrics.reset();
			
		}
		
		
		initialized = true;
	}
	
	@Override
	public boolean execute() {
		if(initialized == false)
		{
			init();
			return false;
		}
		
		double f = 0.;
		double r = 0.;
		double t = 0.;
		double m = 0.;
		
		double stddevr = 0;
		double stddevt = 0;
		double stddevm = 0;
		
		int l = 0;
		
		for(int i = 0; i < Network.size(); i++)
		{
			Node n = Network.get(i);
			
			MetricsProtocol metrics = (MetricsProtocol) n.getProtocol(metrics_pid);
			f += metrics.fractionWithoutLeader();
			
			r += metrics.meanElectionRate();
		
			
			t += metrics.meanElectionTime();
			
			
			m += metrics.meanMessageOverhead();
			
		}
		for(int i = 0; i < Network.size(); i++)
		{
			Node n = Network.get(i);	
			MetricsProtocol metrics = (MetricsProtocol) n.getProtocol(metrics_pid);	
			
			stddevr += (metrics.meanElectionRate() - (r /(double)Network.size()) * (metrics.meanElectionRate() - (r /(double)Network.size())));
			stddevt += (metrics.meanElectionTime() - (t / (double)Network.size())) * (metrics.meanElectionTime() - (t / (double)Network.size()));
			stddevm += (metrics.meanMessageOverhead() - (m / (double)Network.size())) * (metrics.meanMessageOverhead() - (m / (double)Network.size()));
			
		}
		f /= (double) Network.size();
		
		r /= (double) Network.size();
		stddevr /= (double) Network.size();

		
		System.out.println("t " + t);

		t /= (double) Network.size();
		stddevt /= (double) Network.size();
		
		System.out.println("m : " + m);
		m /= (double) Network.size();
		stddevm /= (double) Network.size();
		
		
		System.out.println("Number of nodes : " + Network.size());
		System.out.println("Fraction of time without a leader : " + f * 100 + "%");
		System.out.println("Mean Election Rate : " + r + " elections per minute");
		System.out.println("Election Rate standard deviation: " + Math.sqrt(stddevr));
		System.out.println("Mean Election Time : " + t);
		System.out.println("Election Time standard deviation: " + Math.sqrt(stddevt));
		System.out.println("Mean message Overhead : " + m);
		System.out.println("Message overhead standard deviation: " + Math.sqrt(stddevm));
		
		return true;
	}

}
