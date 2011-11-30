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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

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
	int hisRank;
	public static int[][] listOfPeersandChunks = new int[peerProcess.nofPeers][peerProcess.nofPieces]; 
	static ArrayList<Integer> haveChunkList = new ArrayList<Integer>();
	static ArrayList<Integer> dontHaveChunkList = new ArrayList<Integer>();


	//	ArrayList<Integer> connectedPeers = new ArrayList<Integer>();
	//	ArrayList<Integer> preferredPeers = new ArrayList<Integer>();
	//	HashMap <Integer, ArrayList<Integer>> connected = new HashMap<Integer, ArrayList<Integer>>() ;

	static boolean[][] isConnected = new boolean [peerProcess.nofPeers][peerProcess.nofPeers];

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
		inChunks = new byte[peerProcess.pieceSize];

		this.start();
	}

	public int getRank(String peerId) {
		int rank=0;
		String st;
		try {
			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
			int count=1; //to calculate myrank
			while((st = in.readLine()) != null) {

				String[] tokens = st.split("\\s+");
				if(peerId.equals(tokens[0])){
					rank = count; 
				}
				count++;
			}
			in.close();
		}
		catch (Exception ex) {
			peerProcess.logger.println(ex.toString());
		}
		return rank;
	}


	public void run() {
		try {
			if(isServer) {
				socket = server.accept(); 
				//				peerProcess.logger.print("Connected from "+socket.getPort());
				//				isConnected[Integer.parseInt(peerProcess.myID)][1]=true;
				ois= new ParallelStream(new ObjectInputStream(socket.getInputStream()));
				oos= new ParallelStream(new ObjectOutputStream(socket.getOutputStream()));

			} else {
				sleep(500);
				socket = new Socket(_host,_port);
				oos= new ParallelStream(new ObjectOutputStream(socket.getOutputStream()));
				ois= new ParallelStream(new ObjectInputStream(socket.getInputStream()));

			}

			

			if(isServer){
				//Update its list about itself
				peerProcess.logger.print("My Rank is " +peerProcess.myRank);
				for(int u=0;u<peerProcess.nofPieces;u++) {
					listOfPeersandChunks[peerProcess.myRank-1][u] = 1;
				}

				//Server sending handshake message
				HandshakeMessage hm = new HandshakeMessage(peerProcess.myID);
				oos.writeObject(hm);

				//Recving handshake message
				HandshakeMessage hmServerRecvd = (HandshakeMessage)ois.readObject();
				synchronized (this) {
					isConnected[hmServerRecvd.peerID][Integer
							.parseInt(peerProcess.myID)] = true;
					printConnections();
				}
				peerProcess.logger.print(peerProcess.myID+" is connected from "+hmServerRecvd.peerID);

				//Sending bitfield message if it has file or any chunk initially
				if(peerProcess.haveFile==1) {
					ActualMessage bitfieldMessage = new ActualMessage(peerProcess.nofPieces);
					//<Set bitfield here>
					peerProcess.logger.print("nofpieces "+peerProcess.nofPieces);
					peerProcess.logger.print("Sending bitfield of size "+ bitfieldMessage.messagePayload.length);
					oos.writeObject(bitfieldMessage);
				}

				//update your own knowledge about chunk you have
				for(int i=0;i<peerProcess.nofPeers;i++) {
					for(int j=0;j<peerProcess.nofPieces;j++) {
						peerProcess.logger.println(listOfPeersandChunks[i][j]+" ");
					}
				}

				while(true) {

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
						//do nothing. already handled this case.


					case 6:
						//request

					case 7:
						// piece
						//Sending file chunks

						//ActualMessage chunk = new ActualMessage(makeChunk(1));
						//oos.writeObject(chunk);
						//peerProcess.logger.println("Sent the chunk " +1);

					}
				}




			} else{

				//Recving handshake message
				HandshakeMessage hmClientRecvd = (HandshakeMessage)ois.readObject();
				
				synchronized (this) {
					isConnected[Integer.parseInt(peerProcess.myID)][hmClientRecvd.peerID] = true;
					printConnections();	
				}
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
						//do nothing
						peerProcess.logger.print(peerProcess.myID+" is choked by "+hmClientRecvd.peerID);

					case 1:
						//unchoke
						//send a request
						break;


					case 2:
						//client wont get interested


					case 3:
						//client wont get not interested


					case 4:
						//have
						//update your list
						//send interested if you want that piece
						break;


					case 5:
						//Recving bitfield message
						peerProcess.logger.print(peerProcess.myID + " recieved bitfield message of size "+ messageRcvd.messagePayload.length + " from " +hmClientRecvd.peerID);
						BitSet myRecvBits = new BitSet(peerProcess.nofPieces);
						myRecvBits = messageRcvd.toBitSet(messageRcvd.messagePayload);

						//Updating the list of the chunks the other peer has
						if(!myRecvBits.isEmpty())
						{
							int maxIndex = peerProcess.nofPieces;
							hisRank = getRank(Integer.toString(hmClientRecvd.peerID));
							int index=0, firstBit=-1;
							while(index<maxIndex && index!=firstBit) {
								int x = myRecvBits.nextSetBit(index);
								listOfPeersandChunks[hisRank-1][x] =1;
								index=x+1;
								firstBit=x;
							}
						}

						//updating your dont have list
						for(int j=0;j<peerProcess.nofPieces;j++) {
							if(listOfPeersandChunks[peerProcess.myRank-1][j]==0) {
								dontHaveChunkList.add(j);
							}

						}


						//updating your havelist
						for(int j=0;j<peerProcess.nofPieces;j++) {
							if(listOfPeersandChunks[peerProcess.myRank-1][j]==1) {
								haveChunkList.add(j);
							}
						}
						boolean sentInterested = false;
						//send interested message if he has any piece you dont
						for(int i=0;i<dontHaveChunkList.size();i++) {
							for (int j=dontHaveChunkList.get(i);j<peerProcess.nofPieces;j++) {
								if(listOfPeersandChunks[hisRank-1][j]==1) {
									ActualMessage interested = new ActualMessage("interested");
									oos.writeObject(interested);
									sentInterested = true;
									j=peerProcess.nofPieces; // break out of inner loop
									i=dontHaveChunkList.size();  //break out of outer loop
								}
							}
						}

						if(!sentInterested) {
							ActualMessage interested = new ActualMessage("notinterested");
							oos.writeObject(interested);
						}

						break;


					case 6:
						//request
						//client wont get request

					case 7:
						// piece
						//chunkNumber = get from payload
						//makePartFile(messageRcvd.messagePayload,chunkNumber);

						//after recieving broadcast have
						haveChunk = true;
						break;

					}

					
//					mergeChunks();
//					peerProcess.logger.print("Merged chunks");

				}
				// close streams and connections
				//						socket.close(); 

			} 
		}catch(Exception e) {
			peerProcess.logger.print(e);

		}       
	}

	public byte[] makeChunk(int chunkNo) {
		if(peerProcess.haveFile ==1) {
			try {

				peerProcess.theFile = new File(peerProcess.fileName);
				assert peerProcess.theFile.exists();
				RandomAccessFile ramFile = new RandomAccessFile(peerProcess.theFile, "r");
				ramFile.seek((long)peerProcess.pieceSize*chunkNo);
				peerProcess.logger.print("Chunk number : "+chunkNo+" Now at offset: " +ramFile.getFilePointer());
				if(peerProcess.pieceSize*(chunkNo+1)>ramFile.length()){
					outChunks = new byte[peerProcess.pieceSize];
					ramFile.read(outChunks, 0, (int) (peerProcess.pieceSize*(chunkNo+1)-ramFile.length()));
					//					peerProcess.logger.print("in IF :"+chunkNo+" "+(peerProcess.pieceSize*(chunkNo+1)-ramFile.length()));
				} else {
					outChunks = new byte[peerProcess.pieceSize];
					ramFile.read(outChunks, 0, peerProcess.pieceSize);
				}
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
			fos = null;
		} catch (Exception e) {

			peerProcess.logger.println(e.toString());
		}

	}

	public void mergeChunks() {
		try {
			File outputFile = new File("output.dat");

			FileOutputStream opfos = new FileOutputStream(outputFile,false);
			for(int j=0;j<peerProcess.nofPieces;j++) {

				String partNameHere = j + ".part";
				peerProcess.logger.print("Merging piece :"+partNameHere+" /"+peerProcess.nofPieces);


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

	private void printConnections(){
		String toPrint = new String();
		for(boolean[] a:isConnected){
			for(boolean b:a){
				toPrint+=b+",";
			}
			toPrint+="\n";
		}
		peerProcess.logger.print("\nisConnected\n"+toPrint);
	}

}