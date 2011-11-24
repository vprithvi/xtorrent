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
	public static WriteLog logger;
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
			logger.println(ex.toString());
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
			logger.println(ex.toString());
		}		

	}
	
	//Constructor
	public peerProcess(String arg) throws Exception {
		logger = new WriteLog(arg);
		myID = arg;
		getPeerInfo();
		getCommonConfig();
		server = new ServerSocket(myPort);
		logger.println(myID+": Server listening on port " + myPort);
		this.start();
	}

	//Overriding run method
	public void run() {
		System.out.println("in run");
		logger.print("in run");
		int times = 1;
		while(times > 0) {
		times--;	
			try {
				logger.println("Waiting for connections.");
				Connect c = new Connect(server);
			} catch(Exception e) {
		        peerProcess.logger.println(e.getMessage());
				
			}
			
			try{
		for(int x=0;x<myPeerInfo.size();x++) {
		        logger.println("Created a new client with "+myPeerInfo.elementAt(x).peerAddress+" "+Integer.parseInt(myPeerInfo.elementAt(x).peerPort));	
				Socket socket = new Socket(myPeerInfo.elementAt(x).peerAddress,Integer.parseInt(myPeerInfo.elementAt(x).peerPort));
				new Client(socket);}
			}catch(Exception e){
		        peerProcess.logger.println(e.getMessage());
				
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
			logger.println(peer.peerId+" "+peer.peerAddress+" "+peer.peerPort);

		}
		logger.println( p.myID);
		logger.println( p.haveFile);
		logger.println (p.nofPreferredNeighbour);
		logger.println (p.unchokingInterval);
		logger.println (p.opUnchokingInterval);
		logger.println (p.fileName);
		logger.println (p.fileSize);
		logger.println (p.pieceSize);
		logger.println (p.nofPieces);
		 */
//		new peerProcess();
	}
}

class Connect extends Thread {
//Socket client = server.accept();
	private Socket client = null;
	private ServerSocket server = null;
	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;

	public Connect() {}

	public Connect(ServerSocket serverSocket) {
//				peerProcess.logger.println("Accepted a connection from: "+
//						client.getInetAddress());
//		client = clientSocket;
		server = serverSocket;
		this.start();
	}	
	   public void run() {
		      try {
		    	 client = server.accept();
		  
		 		try {
					ois = new ObjectInputStream(client.getInputStream());
					oos = new ObjectOutputStream(client.getOutputStream());
				} catch(Exception e1) {
					try {
						client.close();
					}catch(Exception e) {
						peerProcess.logger.println(e.getMessage());
					}
					return;
				}

		    	 
		    	 oos.writeObject("Test String");
		         oos.flush();
		         // close streams and connections
		         ois.close();
		         oos.close();
		         client.close(); 
		      } catch(Exception e) {
		        peerProcess.logger.println(e.getMessage());
		    	  
		      }       
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
		        peerProcess.logger.print(socket.getPort()+"Client :Waiting for string");
		        temp = (String) ois.readObject();
		        peerProcess.logger.print("Got: " + temp);
		        oos.close();
		        ois.close();
		      } catch(Exception e) {
		        peerProcess.logger.println(e.getMessage());
		      }
		   }

}


