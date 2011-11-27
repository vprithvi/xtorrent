import java.io.Serializable;
import java.util.BitSet;


public class HandshakeMessage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1559529282860887021L;
	String header;
	int peerID;
	BitSet zeroBits;
	
	HandshakeMessage(String theID) {
		header = "CEN5501C2008SPRING";
		peerID = Integer.parseInt(theID);
		zeroBits = new BitSet(80);
	}
	
	

}
