import java.util.BitSet;


public class HandshakeMessage {
	String header;
	int peerID;
	BitSet zeroBits;
	
	HandshakeMessage(String theID) {
		header = "CEN5501C2008SPRING";
		peerID = Integer.parseInt(theID);
		zeroBits = new BitSet(80);
	}
	
	

}
