import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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

			//			in.close();
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

			//			in.close();
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
		System.out.println("Server: in run");
		logger.print("in run");
		int times = 6;
		while(times > 0) {
			times--;	
			try {
				logger.println("Server: Waiting for connections.");
				Connect c = new Connect(server);
			} catch(Exception e) {
				peerProcess.logger.println(e.getMessage());

			}

			try{
				for(int x=0;x<myPeerInfo.size();x++) {
					if (!myPeerInfo.elementAt(x).peerId.equals(myID)) {
						//					logger.println("Peerinfo size"+myPeerInfo.size());
						logger.println("Created a new client with "
								+ myPeerInfo.elementAt(x).peerAddress + " "
								+ Integer.parseInt(myPeerInfo.elementAt(x).peerPort));
						//					Socket socket = new Socket(myPeerInfo.elementAt(x).peerAddress,Integer.parseInt(myPeerInfo.elementAt(x).peerPort));
						Connect clientthread = new Connect(myPeerInfo.elementAt(x).peerAddress,
								Integer.parseInt(myPeerInfo.elementAt(x).peerPort));
						logger.println("crossed client" + myPeerInfo.elementAt(x).peerAddress
								+ " on " + myPeerInfo.elementAt(x).peerPort);
					}
				}
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
	private Socket socket = null;
	private ServerSocket server = null;
	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;
	String _host = null;
	int _port = 0;
	boolean isServer = false;
	public Connect() {}

	public Connect(ServerSocket serverSocket) {
		//				peerProcess.logger.println("Accepted a connection from: "+
		//						client.getInetAddress());
		//		client = clientSocket;
		server = serverSocket;
		isServer = true;
		this.start();
	}
	
	public Connect(String host, int port){
	_host = host;
	_port = port;
	this.start();
	}
	public void run() {
		try {
			if(isServer) {
				socket = server.accept(); 
			} else {
				socket = new Socket(_host,_port);
			}
			peerProcess.logger.println("Server: Accepted connection isServer"+isServer);

			try {
				ois = new ObjectInputStream(socket.getInputStream());
				oos = new ObjectOutputStream(socket.getOutputStream());
			} catch(Exception e1) {
				try {
					socket.close();
				}catch(Exception e) {
					peerProcess.logger.println(e.getMessage());
				}
				return;
			}

			oos.writeObject("Test String from "+peerProcess.myID+" and port "+peerProcess.myPort);
			oos.flush();
			
			peerProcess.logger.print("Got a message !"+(String) ois.readObject()+"||||INFO: LocalPort "+socket.getLocalPort()+" RemotePort: "+socket.getPort());
			
			// close streams and connections
						ois.close();
						oos.close();
						socket.close(); 
		} catch(Exception e) {
			peerProcess.logger.println(e.getMessage());

		}       
	}

}

class Client extends Thread{
	ObjectOutputStream oos = null;
	ObjectInputStream ois = null;
	Socket socket = null;
	String _host = null;
	int _port = 0;
	public void run(){
		try {
			peerProcess.logger.print("Client: in RUN method :"+peerProcess.myID);
			socket = new Socket(_host,_port);
			peerProcess.logger.print("Client: Created socket in client :"+peerProcess.myID);
		} catch (Exception e) {
			peerProcess.logger.print(e.getMessage());
		}
		String temp = new String ();
		try {
			// open a socket connection
			peerProcess.logger.print(socket.getPort()+"Client: Going to open object streams");
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
	public Client(String host, int port){
		_host = host;
		_port = port;
		peerProcess.logger.println("in Client code");
		this.start();
	}

}


