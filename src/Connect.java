import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Connect extends Thread {
	private Socket socket = null;
	private ServerSocket server = null;
	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;
	private static ParallelStream pos = null;
	private static ParallelStream pis = null;
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
				pis= new ParallelStream(ois);
				pos= new ParallelStream(oos);

			} else {
				socket = new Socket(_host,_port);
				oos = new ObjectOutputStream(socket.getOutputStream());
				ois = new ObjectInputStream(socket.getInputStream());
				pos= new ParallelStream(oos);
				pis= new ParallelStream(ois);

			}
			

				if(isServer){
				
//				pis= new ParallelStream(ois);
//				pos= new ParallelStream(oos);
				
				
				//Server sending handshake message
				HandshakeMessage hm = new HandshakeMessage(peerProcess.myID);
//				oos.writeObject(hm);
//				oos.flush();
				
				pos.writeObject(hm);
				
				//Recving handshake message
//				HandshakeMessage hmServerRecvd = (HandshakeMessage)ois.readObject();
				HandshakeMessage hmServerRecvd = (HandshakeMessage)pis.readObject();
				peerProcess.logger.print(peerProcess.myID+" is connected from "+hmServerRecvd.peerID);
			} else{
//				pos= new ParallelStream(oos);
//				pis= new ParallelStream(ois);
				
				
				//Recving handshake message
				HandshakeMessage hmClientRecvd = (HandshakeMessage)pis.readObject();
				peerProcess.logger.print(peerProcess.myID + " makes a connection to "+hmClientRecvd.peerID);
				
				//Client Sending handshake message
				HandshakeMessage hm2 = new HandshakeMessage(peerProcess.myID);
//				oos.writeObject(hm2);
//				oos.flush();
				pos.writeObject(hm2);
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