package projet;

import java.util.ArrayList;
import java.util.List;

import peersim.core.Node;


public class ElectionProtocolImpl implements ElectionProtocol {
	

	public ElectionProtocolImpl(String prefix)
	{
		//TODO	
	}
	
	@Override
	public Object clone(){
		ElectionProtocol ep = null;
		try { ep = (ElectionProtocol) super.clone();
			;
		
		}
		catch( CloneNotSupportedException e ) {} // never happens
		return ep;
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		// TODO Auto-generated method stub

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
