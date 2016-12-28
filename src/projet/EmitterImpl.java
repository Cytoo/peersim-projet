package projet;

import peersim.core.Node;

public class EmitterImpl implements Emitter {

	
	public EmitterImpl(String prefix)
	{
		//TODO coder
	}
	
	@Override
	public Object clone() {
		EmitterImpl em = null;
		try { em = (EmitterImpl) super.clone();
		//TODO implement;
		
		}
		catch( CloneNotSupportedException e ) {} // never happens
		return em;
	}

	@Override
	public void emit(Node host, Message msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getLatency() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getScope() {
		// TODO Auto-generated method stub
		return 0;
	}

}
