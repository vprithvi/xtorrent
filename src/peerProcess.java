import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Vector;


public class peerProcess {

	public static String myID;
	public Vector<RemotePeerInfo> myPeerInfo;
	static int haveFile;
	static int nofPreferredNeighbour;
	static int unchokingInterval;
	static int opUnchokingInterval;
	static String fileName;
	static int fileSize;
	static int pieceSize;
	static int nofPieces;
	
	
	//Method to read PeerInfo.cfg
	public void getPeerInfo() {
			String st;
			myPeerInfo = new Vector<RemotePeerInfo>();
			try {
				BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
				while((st = in.readLine()) != null) {
					
					 String[] tokens = st.split("\\s+");
					 myPeerInfo.addElement(new RemotePeerInfo(tokens[0], tokens[1], tokens[2]));
					 
					 if(myID.equals(tokens[0])){
						 haveFile = Integer.parseInt(tokens[3]);
					 }
				}
				
				in.close();
			}
			catch (Exception ex) {
				System.out.println(ex.toString());
			}
	}
	
	public void getCommonConfig(){
		String cfg;
		ArrayList<String> configInfo = new ArrayList<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));
			while((cfg = in.readLine()) != null) {
				
				 String[] tokens = cfg.split("\\s+");
				 configInfo.add(tokens[1]);
			}
			nofPreferredNeighbour = Integer.parseInt(configInfo.get(0));
			unchokingInterval = Integer.parseInt(configInfo.get(1));
			opUnchokingInterval = Integer.parseInt(configInfo.get(2));
			fileName = configInfo.get(3);
			fileSize = Integer.parseInt(configInfo.get(4));
			pieceSize = Integer.parseInt(configInfo.get(5));
			nofPieces = (int)Math.ceil((double)fileSize/pieceSize);
					
			in.close();
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}		
		
	}
	
	public static void main(String[] args){
		peerProcess p = new peerProcess();
		p.myID = args[0];
		p.getPeerInfo();
		p.getCommonConfig();
		
		
		/* test to print parameters
		 * 
		for(int x=0;x<p.myPeerInfo.size();x++) {
			RemotePeerInfo peer = (RemotePeerInfo)p.myPeerInfo.elementAt(x);
			System.out.println(peer.peerId+" "+peer.peerAddress+" "+peer.peerPort);
			
		}
		System.out.println( p.myID);
		System.out.println( p.haveFile);
		System.out.println (p.nofPreferredNeighbour);
		System.out.println (p.unchokingInterval);
		System.out.println (p.opUnchokingInterval);
		System.out.println (p.fileName);
		System.out.println (p.fileSize);
		System.out.println (p.pieceSize);
		System.out.println (p.nofPieces);
		*/
		
	}
	
	
}

