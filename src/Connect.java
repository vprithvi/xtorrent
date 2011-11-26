import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Connect extends Thread {
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
//		try {
//			socket = server.accept();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
		this.start();
	}
	
	public Connect(String host, int port){
	_host = host;
	_port = port;
//				try {
//					socket = new Socket(_host,_port);
//				} catch (UnknownHostException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
	this.start();
	}
	public void run() {
		try {
			if(isServer) {
				socket = server.accept(); 
				peerProcess.logger.println("Server Accept crossed lp:"+ socket.getLocalPort()+" p"+socket.getPort());
				ois = new ObjectInputStream(socket.getInputStream());
				oos = new ObjectOutputStream(socket.getOutputStream());
			} else {
				socket = new Socket(_host,_port);
				peerProcess.logger.println("New Server crossed lp:"+ socket.getLocalPort()+" p"+socket.getPort());
				oos = new ObjectOutputStream(socket.getOutputStream());
				ois = new ObjectInputStream(socket.getInputStream());
			}
			peerProcess.logger.println("Server: Accepted connection isServer:"+isServer);

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
				peerProcess.logger.print("Sent Message");
				oos.writeObject("Test String from "+peerProcess.myID+" and port "+peerProcess.myPort);
				oos.flush();
			} else{
				peerProcess.logger.print("Waiting for Message");
				peerProcess.logger.print("Got a message !"+(String) ois.readObject()+"||||INFO: LocalPort "+socket.getLocalPort()+" RemotePort: "+socket.getPort());
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