import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;



public class Client extends Thread{
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
