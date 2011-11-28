import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.BitSet;

public class Connect extends Thread {
	private Socket socket = null;
	private ServerSocket server = null;
	private ParallelStream oos = null;
	private ParallelStream ois = null;

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
				ois= new ParallelStream(new ObjectInputStream(socket.getInputStream()));
				oos= new ParallelStream(new ObjectOutputStream(socket.getOutputStream()));

			} else {
				socket = new Socket(_host,_port);
				oos= new ParallelStream(new ObjectOutputStream(socket.getOutputStream()));
				ois= new ParallelStream(new ObjectInputStream(socket.getInputStream()));

			}
			

				if(isServer){
				
				//Server sending handshake message
				HandshakeMessage hm = new HandshakeMessage(peerProcess.myID);
				oos.writeObject(hm);
				
				//Recving handshake message
				HandshakeMessage hmServerRecvd = (HandshakeMessage)ois.readObject();
				peerProcess.logger.print(peerProcess.myID+" is connected from "+hmServerRecvd.peerID);
				
				//Sending bitfield message
				ActualMessage bitfieldMessage = new ActualMessage(peerProcess.nofPieces);
				//<Set bitfield here>
				BitSet myBits = new BitSet(peerProcess.nofPieces);
				myBits = bitfieldMessage.toBitSet(bitfieldMessage.messagePayload);
				//<modify the bits here>
				bitfieldMessage.messagePayload = bitfieldMessage.toByteArray(myBits);
				oos.writeObject(bitfieldMessage);

				
				
			} else{
			
				
				//Recving handshake message
				HandshakeMessage hmClientRecvd = (HandshakeMessage)ois.readObject();
				peerProcess.logger.print(peerProcess.myID + " makes a connection to "+hmClientRecvd.peerID);
				
				//Client Sending handshake message
				HandshakeMessage hm2 = new HandshakeMessage(peerProcess.myID);
				oos.writeObject(hm2);
				
				//Recving bitfield message
				ActualMessage bitfieldMessageRcvd = (ActualMessage)ois.readObject();
				peerProcess.logger.print(peerProcess.myID + " recieved bitfield message of size "+ bitfieldMessageRcvd.messagePayload.length + " from " +hmClientRecvd.peerID);

			}
			// close streams and connections
//						socket.close(); 
			
		} catch(Exception e) {
			peerProcess.logger.println(e.getStackTrace().toString());

		}       
	}

}