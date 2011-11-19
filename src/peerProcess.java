import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;


public class peerProcess {
	String pi;
	ArrayList<String> peerInfo = new ArrayList<String>();
	
	public void getPeerInfo() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
			while((pi = in.readLine()) != null) {
				 String[] tokens = pi.split("\\s+");
			     peerInfo.add(tokens[0]);
			     
				 //peerInfoVector.addElement(new RemotePeerInfo(tokens[0], tokens[1], tokens[2]));
			
			}
			
			in.close();
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}
}
