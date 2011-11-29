import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.BitSet;
import java.util.Random;

public class Connect extends Thread {
	private Socket socket = null;
	private ServerSocket server = null;
	private ParallelStream oos = null;
	private ParallelStream ois = null;
	
	public byte[] inChunks;
	public byte[] outChunks;
	public long chunkNumber;
	
	

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
				
				//Sending bitfield message
				ActualMessage bitfieldMessage = new ActualMessage(peerProcess.nofPieces);
				//<Set bitfield here>
				BitSet myBits = new BitSet(peerProcess.nofPieces);
				myBits = bitfieldMessage.toBitSet(bitfieldMessage.messagePayload);
				//<modify the bits here>
				bitfieldMessage.messagePayload = bitfieldMessage.toByteArray(myBits);
				oos.writeObject(bitfieldMessage);
				
				
				//Sending file chunks
				for(int j=0;j<peerProcess.nofPieces;j++) {
//					ActualMessage chunk = new ActualMessage(makeChunk(j));
					ActualMessage chunk = new ActualMessage(makeChunk(j));
					Random randomGenerator = new Random();
					chunk.setChunkid(randomGenerator.nextInt(1000));
//					makePartFile(chunk.messagePayload,j);
//					peerProcess.logger.print("\n\nWrote to stream\n"+new String(chunk.messagePayload)+"\n");
					oos.writeObject(chunk);
					peerProcess.logger.println("Sent the chunk " +j + "with chunkid :" + chunk.getChunkid());
				}
					
				
				
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
				
				//Recv chunk
				for(int j=0;j<peerProcess.nofPieces;j++) {
					ActualMessage recvdChunk = (ActualMessage)ois.readObject();
					makePartFile(recvdChunk.messagePayload,j);
//					makePartFile(recvdChunk.stringPayload.getBytes(),j);
					peerProcess.logger.print("\n\nGot from stream BYTE ARRAY\n"+new String(recvdChunk.messagePayload)+"\n");
					peerProcess.logger.print("\n\nGot from stream STRING \n"+new String(recvdChunk.messagePayload)+"\n");
					peerProcess.logger.print("Recvd a chunk "+j+"with chunk id"+recvdChunk.getChunkid());
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
//		System.Text.Encoding enc = System.Text.Encoding.ASCII
		if(peerProcess.haveFile ==1) {
			try {
			peerProcess.theFile = new File(peerProcess.fileName);
			RandomAccessFile ramFile = new RandomAccessFile(peerProcess.theFile, "r");
			ramFile.seek((long)peerProcess.pieceSize*chunkNo);
			peerProcess.logger.print("Chunk number : "+chunkNo+" Now at offset: " +ramFile.getFilePointer());
			ramFile.read(outChunks, 0, peerProcess.pieceSize);
//			peerProcess.logger.print("\nRead \n"+new String(outChunks)+"\n");
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
		} catch (Exception e) {
			
			peerProcess.logger.println(e.toString());
		}
		
	}
	
	public void mergeChunks() {
		try {
			File outputFile = new File("output.dat");
			FileOutputStream opfos = new FileOutputStream(outputFile,false);
			PrintWriter op = new PrintWriter (opfos);
			for(int j=0;j<peerProcess.nofPieces;j++) {
				String partNameHere = j + ".part";
				peerProcess.logger.print("Merging piece :"+partNameHere+" /"+peerProcess.nofPieces);

				
				File partFile = new File(partNameHere);
				FileInputStream pffis = new FileInputStream(partFile);
				byte[] fb = new byte[(int)partFile.length()];
				pffis.read(fb, 0, (int)partFile.length());
				opfos.write(fb);
				opfos.flush();
				fb = null;
				pffis.close();
				pffis =null;
			}
			op.close();
			opfos.close();
		} catch (Exception e) {
			
			peerProcess.logger.println(e.toString());
		}
		
		
	}

}