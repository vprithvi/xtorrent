import java.io.BufferedReader;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;


public class peerProcess extends Thread {

	public static String myID;
	public static int myPort;
	
	public Vector<RemotePeerInfo> myPeerInfo;
	static int haveFile;
	static int nofPreferredNeighbour;
	static int unchokingInterval;
	static int opUnchokingInterval;
	static String fileName;
	static int fileSize;
	static int pieceSize;
	static int nofPieces;

	private ServerSocket server;
	
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
					myPort = Integer.parseInt(tokens[2]);
				}
			}

			in.close();
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}

	//Method to read CommonInfo.cfg
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
	
	//Constructor
	public peerProcess(String arg) throws Exception {
		myID = arg;
		getPeerInfo();
		getCommonConfig();
		server = new ServerSocket(myPort);
		System.out.println(myID+": Server listening on port " + myPort);
		this.start();
	}

	//Overriding run method
	public void run() {
		while(true) {
			
			try {
				System.out.println("Waiting for connections.");
				Socket client = server.accept();
				System.out.println("Accepted a connection from: "+
						client.getInetAddress());
				Connect c = new Connect(client);
			} catch(Exception e) {}
			
			try{
		for(int x=0;x<myPeerInfo.size();x++) {
			
				Socket socket = new Socket(myPeerInfo.elementAt(x).peerAddress,Integer.parseInt(myPeerInfo.elementAt(x).peerPort));
				new Client(socket);}
			}catch(Exception e){
				
			}
			
		}
	}
	
	//Main method
	public static void main(String[] args)  throws Exception {
	    
		peerProcess p = new peerProcess(args[0]);


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
//		new peerProcess();
	}
}

class Connect extends Thread {
	private Socket client = null;
	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;

	public Connect() {}

	public Connect(Socket clientSocket) {
		client = clientSocket;
		try {
			ois = new ObjectInputStream(client.getInputStream());
			oos = new ObjectOutputStream(client.getOutputStream());
		} catch(Exception e1) {
			try {
				client.close();
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}
			return;
		}
	}	
	   public void run() {
		      try {
		         oos.writeObject("Test String");
		         oos.flush();
		         // close streams and connections
		         ois.close();
		         oos.close();
		         client.close(); 
		      } catch(Exception e) {}       
		   }

}

class Client {
	      ObjectOutputStream oos = null;
	      ObjectInputStream ois = null;
	      Socket socket = null;
	   public Client(Socket toConnect){
		      String temp = new String ();
		      try {
		        // open a socket connection
//		        socket = new Socket("localhost", 3000);
		    	  socket = toConnect;
//		        socket.bind(new InetSocketAddress("localhost",3005));
		        // open I/O streams for objects
		        oos = new ObjectOutputStream(socket.getOutputStream());
		        ois = new ObjectInputStream(socket.getInputStream());
		        // read an object from the server
		        temp = (String) ois.readObject();
		        System.out.print("Got: " + temp);
		        oos.close();
		        ois.close();
		      } catch(Exception e) {
		        System.out.println(e.getMessage());
		      }
		   }

}


