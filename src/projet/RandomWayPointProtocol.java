package projet;

import peersim.config.Configuration;
import peersim.core.CommonState;
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
	private int posx;
	private int posy;

	
	public RandomWayPointProtocol(String prefix)
	{
		String tmp[]=prefix.split("\\.");
		protocol_id=Configuration.lookupPid(tmp[tmp.length-1]);
		
		vmin=Configuration.getInt(prefix+"."+PAR_VMIN);
		vmax=Configuration.getInt(prefix+"."+PAR_VMAX);
		pause=Configuration.getInt(prefix+"."+PAR_PAUSE);
		width=Configuration.getInt(prefix+"."+PAR_WIDTH);
		height=Configuration.getInt(prefix+"."+PAR_HEIGHT);
		
		posx = CommonState.r.nextInt(width);
		posy = CommonState.r.nextInt(height);
		System.out.println("x : " + posx + " y : " + posy);
		
		chooseNewDestination();	
	}
	
	@Override
	public Object clone() {
		RandomWayPointProtocol rp = null;
		try { rp = (RandomWayPointProtocol) super.clone();
			rp.posx = CommonState.r.nextInt(width);
			rp.posy = CommonState.r.nextInt(height);
			System.out.println("Cloned .x : " + rp.posx + " y : " + rp.posy);
			rp.chooseNewDestination();
			System.out.println("Cloned destination .x : " + rp.destinx + " y : " + rp.destiny + "speed" + rp.speed);
		}
		catch( CloneNotSupportedException e ) {} // never happens
		return rp;
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		if(protocol_id != pid){
			throw new RuntimeException("Receive Message for wrong protocol");
		}
		
		double distance = Math.sqrt(((destinx - posx) * (destinx - posx) + (destiny - posy) * (destiny - posy)));
		
		if(distance < speed)
		{
			posx = destinx;
			posy = destiny;
			
			//on est arrivé
			chooseNewDestination();
			EDSimulator.add(pause * 1000, null, node, pid);
		}
		else
		{
			posx += (destinx - posx) * speed / distance; 
			posy += (destiny - posy) * speed / distance; 
			EDSimulator.add(1000, null, node, pid);
		}
		
		
	}

	@Override
	public double getY() {
		// TODO Auto-generated method stub
		return posy;
	}

	@Override
	public double getX() {
		// TODO Auto-generated method stub
		return posx;
	}

	@Override
	public int getMaxSpeed() {
		// TODO Auto-generated method stub
		return vmax;
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
		return pause;
	}
	private void chooseNewDestination()
	{
		speed = CommonState.r.nextDouble() * (vmax - vmin) + vmin;
		destinx = CommonState.r.nextInt(width);
		destiny = CommonState.r.nextInt(height);	
	}

}
