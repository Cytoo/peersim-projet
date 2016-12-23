package projet;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class RandomWayPointProtocol implements PositionProtocol {

	private static final String PAR_VMIN = "vmin";
	private static final String PAR_VMAX = "vmax";
	private static final String PAR_PAUSE = "pausetime";
	private static final String PAR_WIDTH = "width";
	private static final String PAR_HEIGHT = "height";
	
	private final int vmin;
	private final int vmax;
	private final int pause;
	private final int width;
	private final int height;
	
	private final int protocol_id;
	
	private double speed;
	private int destinx;
	private int destiny;

	
	public RandomWayPointProtocol(String prefix)
	{
		String tmp[]=prefix.split("\\.");
		protocol_id=Configuration.lookupPid(tmp[tmp.length-1]);
		
		vmin=Configuration.getInt(prefix+"."+PAR_VMIN);
		vmax=Configuration.getInt(prefix+"."+PAR_VMAX);
		pause=Configuration.getInt(prefix+"."+PAR_PAUSE);
		width=Configuration.getInt(prefix+"."+PAR_WIDTH);
		height=Configuration.getInt(prefix+"."+PAR_HEIGHT);
		
	
		chooseNewDestination();	
	}
	
	@Override
	public Object clone() {
		RandomWayPointProtocol rp = null;
		try { rp = (RandomWayPointProtocol) super.clone();
		
		
		}
		catch( CloneNotSupportedException e ) {} // never happens
		return rp;
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		if(protocol_id != pid){
			throw new RuntimeException("Receive Message for wrong protocol");
		}
		
		//EDSimulator.add(pause * 1000, null, node, pid);
		
	}

	@Override
	public double getY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMaxX() {
		return width;
	}

	@Override
	public double getMaxY() {
		// TODO Auto-generated method stub
		return height;
	}

	@Override
	public int getTimePause() {
		// TODO Auto-generated method stub
		return 0;
	}
	private void chooseNewDestination()
	{
		speed = CommonState.r.nextDouble() * (vmax - vmin) + vmin;
		destinx = CommonState.r.nextInt() % width;
		destiny = CommonState.r.nextInt() % height;	
	}

}
