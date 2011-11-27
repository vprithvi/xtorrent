import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Connect extends Thread {
	private Socket socket = null;
	private ServerSocket server = null;
	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;
	String _host = null;
	int _port = 0;
	boolean isServer = false;
	public Connect() {}

	public Connect(ServerSocket serverSocket) {
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
				ois = new ObjectInputStream(socket.getInputStream());
				oos = new ObjectOutputStream(socket.getOutputStream());
			} else {
				socket = new Socket(_host,_port);
				oos = new ObjectOutputStream(socket.getOutputStream());
				ois = new ObjectInputStream(socket.getInputStream());
			}
			
			try {
				oos = new ObjectOutputStream(socket.getOutputStream());
				ois = new ObjectInputStream(socket.getInputStream());
			} catch(Exception e1) {
				try {
//					socket.close();
				}catch(Exception e) {
					peerProcess.logger.println(e.getMessage());
				}
//				return;
			}
            
			if(isServer){
				
				//Server sending handshake message
				HandshakeMessage hm = new HandshakeMessage(peerProcess.myID);
				oos.writeObject(hm);
				oos.flush();
				
				//Recving handshake message
				HandshakeMessage hmServerRecvd = (HandshakeMessage)ois.readObject();
				peerProcess.logger.print(peerProcess.myID+" is connected from "+hmServerRecvd.peerID);
			} else{
				//Recving handshake message
				HandshakeMessage hmClientRecvd = (HandshakeMessage)ois.readObject();
				peerProcess.logger.print(peerProcess.myID + " makes a connection to "+hmClientRecvd.peerID);
				
				//Client Sending handshake message
				HandshakeMessage hm2 = new HandshakeMessage(peerProcess.myID);
				oos.writeObject(hm2);
				oos.flush();
			}
			// close streams and connections
//						ois.close();
//						oos.close();
//						socket.close(); 
		} catch(Exception e) {
			peerProcess.logger.println(e.getMessage());

		}       
	}

}