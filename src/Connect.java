import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.BitSet;

public class Connect extends Thread {
	private Socket socket = null;
	private ServerSocket server = null;
	private ParallelStream oos = null;
	private ParallelStream ois = null;
	
	public byte[] inChunks;
	public byte[] outChunks;
	public long chunkNumber;
	public boolean complete = false;
	public boolean haveChunk = false;
	
	

	String _host = null;
	int _port = 0;
	boolean isServer = false;
	public Connect() {}

	public Connect(ServerSocket serverSocket) {
		server = serverSocket;
		isServer = true;
		outChunks = new byte[peerProcess.pieceSize];
		this.start();
	}
	
	public Connect(String host, int port){
		_host = host;
		_port = port;
		inChunks = new byte[peerProcess.pieceSize];
		this.start();
	}
	

	public void run() {
		try {
			if(isServer) {
				socket = server.accept(); 
				ois= new ParallelStream(new ObjectInputStream(socket.getInputStream()));
				oos= new ParallelStream(new ObjectOutputStream(socket.getOutputStream()));

			} else {
				sleep(500);
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
				
				//Sending bitfield message if it has file or any chunk initially
				if(peerProcess.haveFile==1 || haveChunk) {
					ActualMessage bitfieldMessage = new ActualMessage(peerProcess.nofPieces);
					//<Set bitfield here>
					BitSet myBits = new BitSet(peerProcess.nofPieces);
					myBits = bitfieldMessage.toBitSet(bitfieldMessage.messagePayload);
					//<modify the bits here>
					bitfieldMessage.messagePayload = bitfieldMessage.toByteArray(myBits);
					oos.writeObject(bitfieldMessage);
				}
				while(true) {
				//Sending file chunks
				
					ActualMessage chunk = new ActualMessage(makeChunk(1));
					oos.writeObject(chunk);
					peerProcess.logger.println("Sent the chunk " +1);
					
				}	
				
					
				
				
			} else{
			
				
				//Recving handshake message
				HandshakeMessage hmClientRecvd = (HandshakeMessage)ois.readObject();
				peerProcess.logger.print(peerProcess.myID + " makes a connection to "+hmClientRecvd.peerID);
				
				//Client Sending handshake message
				HandshakeMessage hm2 = new HandshakeMessage(peerProcess.myID);
				oos.writeObject(hm2);
				
				while(!complete) {
				
				//Recving message
				ActualMessage messageRcvd = (ActualMessage)ois.readObject();
				switch(messageRcvd.messageType) {
				case 0:
					//choke
					
				case 1:
					//unchoke
					
					
				case 2:
					//interested
					
					
				case 3:
					//not interested
					
					
				case 4:
					//have
					
					
				case 5:
					//bitfield
					peerProcess.logger.print(peerProcess.myID + " recieved bitfield message of size "+ messageRcvd.messagePayload.length + " from " +hmClientRecvd.peerID);
					
				case 6:
					//request
					
				case 7:
					// piece
					
					
				}
				
				
				//Recv chunk
			
					ActualMessage recvdChunk = (ActualMessage)ois.readObject();
					makePartFile(recvdChunk.messagePayload,1);
					peerProcess.logger.print("Recvd a chunk "+1);
					
				}
				
				
				mergeChunks();
				peerProcess.logger.print("Merged chunks");

			}
			// close streams and connections
//						socket.close(); 
			
		} catch(Exception e) {
			peerProcess.logger.print(e);
			
		}       
	}
	
	public byte[] makeChunk(int chunkNo) {
		if(peerProcess.haveFile ==1) {
			try {
			peerProcess.theFile = new File(peerProcess.fileName);
			RandomAccessFile ramFile = new RandomAccessFile(peerProcess.theFile, "r");
			ramFile.seek((long)peerProcess.pieceSize*chunkNo);
			ramFile.read(outChunks, 0, peerProcess.pieceSize);
			ramFile.close();
			peerProcess.logger.println("I have the file. SPLIT it into "+outChunks.length+" chunks ");
					
			} catch (Exception e) {
			
				peerProcess.logger.println(e.toString());
			}
			
			
		}
		return outChunks;
	}
	
	public void makePartFile(byte[] inputChunk, int chunkNumber) {
		String partName = chunkNumber + ".part";
		File outputFile = new File(partName);
		try {
			FileOutputStream fos = new FileOutputStream(outputFile);
			fos.write(inputChunk);
			fos.flush();
			fos.close();
			fos = null;
		} catch (Exception e) {
			
			peerProcess.logger.println(e.toString());
		}
		
	}
	
	public void mergeChunks() {
		try {
			File outputFile = new File("output.dat");
			FileOutputStream opfos = new FileOutputStream(outputFile,true);
			for(int j=1;j<peerProcess.nofPieces;j++) {
				String partNameHere = j + ".part";
				File partFile = new File(partNameHere);
				FileInputStream pffis = new FileInputStream(partFile);
				peerProcess.logger.println("Length of partfile "+partFile.length());
				byte[] fb = new byte[(int)partFile.length()];
				peerProcess.logger.println("Length of byte array"+ fb.length);
				pffis.read(fb, 0, (int)partFile.length());
				opfos.write(fb);
				opfos.flush();
				fb = null;
				pffis.close();
				pffis =null;
			}
			opfos.close();
		} catch (Exception e) {
			
			peerProcess.logger.println(e.toString());
		}
		
		
	}

}