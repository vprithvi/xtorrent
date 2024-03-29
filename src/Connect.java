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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.print.CancelablePrintJob;



public class Connect extends Thread {
	private Socket socket = null;
	private ServerSocket server = null;
	private ParallelStream oos = null;
	private ParallelStream ois = null;
	private static ParallelStream consolidated_oos[] = new ParallelStream[peerProcess.nofPeers];
	//	private static int consolidated_index=0;
	static private List<Thread> threads = new ArrayList<Thread>();

	public int chunkNumber;

	static boolean complete = false;
	public static int byeCount =0;
	public boolean haveChunk = false;
	public static boolean choked = true;
	int hisRank;
	public static int[][] listOfPeersandChunks = new int[peerProcess.nofPeers][peerProcess.nofPieces]; 
	static List<Integer> haveChunkList = Collections.synchronizedList(new ArrayList<Integer>());
	static List<Integer> dontHaveChunkList = Collections.synchronizedList(new ArrayList<Integer>());
	static List<Integer> unchokedList = Collections.synchronizedList(new ArrayList<Integer>());
	static List<Integer> chokedList = Collections.synchronizedList(new ArrayList<Integer>());
	static List<Integer> downloadRate = Collections.synchronizedList(new ArrayList<Integer>());
	static List<Integer> peerIDList = Collections.synchronizedList(new ArrayList<Integer>());
	static List<Integer> rankList= Collections.synchronizedList(new ArrayList<Integer>());
	static List<Integer> preferredNeighbors = Collections.synchronizedList(new ArrayList<Integer>());
	static List<Integer> chokedNeighbors = Collections.synchronizedList(new ArrayList<Integer>());
	static List<Integer> interestedList = Collections.synchronizedList(new ArrayList<Integer>());
	static List<Integer> optimisticUnchokeList = Collections.synchronizedList(new ArrayList<Integer>());
	//	List<Integer> a = Collections.synchronizedList(new ArrayList<Integer>());
	static int[] downloadPieces= new int [peerProcess.nofPeers];
	String fileDirectory="peer_"+peerProcess.myID+"/";
	String _host = null;
	int _port = 0;
	boolean isServer = false;

	Timer chokeTimer = new Timer();
	Timer optChokeTimer = new Timer();

	public Connect() {}

	public Connect(ServerSocket serverSocket) {
		server = serverSocket;
		isServer = true;
		threads.add(this);
		this.start();
	}

	public Connect(String host, int port){
		_host = host;
		_port = port;
		threads.add(this);
		this.start();
	}

	synchronized public ArrayList<Integer> removeDuplicates(ArrayList<Integer> a){
		//		synchronized (this) {
		//		ArrayList al = new ArrayList();
		// add elements to al, including duplicates
		HashSet<Integer> hs = new HashSet<Integer>();
		hs.addAll(a);
		a.clear();
		a.addAll(hs);
		return a;
		//		}
	}

	synchronized public List<Integer> removeDuplicates(List<Integer> a){
		synchronized (this) {
			//		ArrayList al = new ArrayList();
			// add elements to al, including duplicates
			HashSet<Integer> hs = new HashSet<Integer>();
			hs.addAll(a);
			a.clear();
			a.addAll(hs);
			return a;
		}
	}

	public void generatePeerIDList() {
		String st;
		try {
			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
			while((st = in.readLine()) != null) {

				String[] tokens = st.split("\\s+");
				peerIDList.add(Integer.parseInt(tokens[0]));
			}
			in.close();
		}
		catch (Exception ex) {
			peerProcess.logger.println(ex.toString());
		}

	}	

	public void generateRankList() {
		String st;
		try {
			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
			while((st = in.readLine()) != null) {

				String[] tokens = st.split("\\s+");
				if(!rankList.contains(getRank(tokens[0]))){
					rankList.add(getRank(tokens[0]));
				}
			}
			in.close();
		}
		catch (Exception ex) {
			peerProcess.logger.println(ex.toString());
		}

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

	public int getRank(int peerId) {
		int rank=0;
		String st;
		try {
			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
			int count=1; //to calculate myrank
			while((st = in.readLine()) != null) {

				String[] tokens = st.split("\\s+");
				if(peerId == Integer.parseInt(tokens[0])){
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

	public int getPeerID(int rank) {
		int peerIDH=0;
		String st;
		try {
			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
			int count=1; //to calculate myrank
			while((st = in.readLine()) != null) {

				String[] tokens = st.split("\\s+");
				if(rank == count){
					peerIDH = Integer.parseInt(tokens[0]); 
				}
				count++;
			}
			in.close();
		}
		catch (Exception ex) {
			peerProcess.logger.println(ex.toString());
		}
		return peerIDH;
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

			//Generate peerIDlist
			generatePeerIDList();
			generateRankList();
			//Unchoke thread

			class unchoker extends TimerTask{

				@Override
				synchronized public void run() {
					//						peerProcess.logger.print("The value of complete is :" + complete);
					if(complete){
						this.cancel();

					}

					try {
						if(interestedList.size()==0){
							//								peerProcess.logger.println("Timer :Empty list");
							return;

						}
						//Reset the preferred Neighbors list
						preferredNeighbors.clear();
						//							peerProcess.logger.print("Timer: interested List"+interestedList.toString());
						int numberToUnchoke = peerProcess.nofPreferredNeighbour;

						while (numberToUnchoke > 0) {
							if(interestedList.size()==0){
								//									peerProcess.logger.println("Timer :Empty list");
								break;

							}
							//check if interested list is empty, if so leave it until someone sends interested

							numberToUnchoke--;

							//get the max from the downloadpieces for a peer who is in interested list
							int max=0, maxIndex=0;
							for(int i = 0; i < interestedList.size(); i++) {
								if(downloadPieces[interestedList.get(i)]>max) {
									max = downloadPieces[interestedList.get(i)];
									maxIndex =i;
								}

							}
							//once peer is selected from list, remove him and add him to the preferredlist
							preferredNeighbors.add(interestedList.get(maxIndex));
							interestedList.remove(maxIndex);


							//Replace unchokelist with prefne
							if(unchokedList.size()>0) {

								//send choke to unchoke list and clear
								//								Sending choke
								for(int i =0; i<consolidated_oos.length; i++){

									if(null!=consolidated_oos[i]&&i!=(peerProcess.myRank-1)&&unchokedList.contains(i)){
										//											peerProcess.logger.print("Timer: Sending choke to "+(i+1)+" who was previously on Unchoke mode");
										consolidated_oos[i].writeObject(new ActualMessage("choke"));
									}
								}

								unchokedList.clear();
								unchokedList.addAll(preferredNeighbors);
								ArrayList<Integer> temp_temp = new ArrayList<Integer>();

								if(unchokedList.size()>0) {
									for(int i=0;i<unchokedList.size();i++) {
										temp_temp.add(getPeerID((unchokedList.get(i))));
									}
								}


								peerProcess.logger.print(" Peer "+ peerProcess.myID+" has the preferred neighbors "+ temp_temp.toString());

								//Sending unchoke
								for(int i =0; i<consolidated_oos.length; i++){

									if(null!=consolidated_oos[i]&&i!=(peerProcess.myRank-1)&&unchokedList.contains(i)){
										//											peerProcess.logger.print("Timer: Sending unchoke to "+(i+1));
										consolidated_oos[i].writeObject(new ActualMessage("unchoke"));
									}
								}


							}


						}
						synchronized(this){
							//								peerProcess.logger.print("Timer: Populated the prefferedNeighbors list: "+preferredNeighbors.toString());
						}
						//Reset the download rate
						for( int b:downloadPieces){
							b=0;
							//							peerProcess.logger.println("The value of b is "+b);
						}


					} catch (Exception e) {
						peerProcess.logger.print(e);
					}
				}
			}

			chokeTimer.scheduleAtFixedRate(new unchoker(), 1, peerProcess.unchokingInterval*1000);


			//Optimistic unchoking


			class optimisticUnchoker extends TimerTask{

				@Override
				public void run() {
					if(complete){
						this.cancel();
					}
					peerIDList=removeDuplicates(peerIDList);
					peerIDList.remove((Object)peerProcess.myID);
					int temp_index =new Random().nextInt(peerIDList.size());
					//						peerProcess.logger.print("Timer temp index is "+ temp_index+"peerlist"+ peerIDList.toString());
					if(temp_index>=0){

						for(int i = getRank(peerIDList.get(temp_index)); i<peerProcess.nofPeers; i = getRank(peerIDList.get(temp_index))){
							if(null!=consolidated_oos[i]&&i!=(peerProcess.myRank-1)&&unchokedList.contains(i)&&i>=0){
								//								peerProcess.logger.print("Timer: Sending optimistic unchoke to "+(i+1));
								consolidated_oos[i].writeObject(new ActualMessage("unchoke"));
								if(optimisticUnchokeList.size()>0) {
									optimisticUnchokeList.clear();
								}
								optimisticUnchokeList.add(i);

								break;
							}
						}
						if(optimisticUnchokeList.size()>0) {
							peerProcess.logger.print(" Peer "+ peerProcess.myID+" has the optimistically unchoked neighbor "+ optimisticUnchokeList.get(0));
						}
					}
				}


			}

			optChokeTimer.scheduleAtFixedRate(new optimisticUnchoker(), 1, peerProcess.opUnchokingInterval);


			//Update its list about itself
			//			peerProcess.logger.print("My Rank is " +peerProcess.myRank);
			//			peerProcess.logger.print("Nofpeersand pieces " +peerProcess.nofPeers+" "+peerProcess.nofPieces);
			if(peerProcess.haveFile==1) {


				for(int u=0;u<peerProcess.nofPieces;u++) {
					listOfPeersandChunks[peerProcess.myRank-1][u] = 1;
				}
			}

			//Server sending handshake message
			HandshakeMessage hm = new HandshakeMessage(peerProcess.myID);
			oos.writeObject(hm);

			//Recving handshake message
			HandshakeMessage hmRecvd = (HandshakeMessage)ois.readObject();
			hisRank = getRank(Integer.toString(hmRecvd.peerID));
			consolidated_oos[hisRank-1]=oos;
			//			peerProcess.logger.print("set consolidated oos of "+ (hisRank-1));
			if(isServer){
				peerProcess.logger.print(peerProcess.myID+" is connected from Peer "+hmRecvd.peerID);
			} else {
				peerProcess.logger.print(peerProcess.myID+" makes a connection to Peer "+hmRecvd.peerID);
			}

			//Sending bitfield message if it has file or any chunk initially
			if(peerProcess.haveFile==1) {
				ActualMessage bitfieldMessage = new ActualMessage(peerProcess.nofPieces);
				//<Set bitfield here>
				//				peerProcess.logger.print("nofpieces "+peerProcess.nofPieces);
				//					peerProcess.logger.print("Sending bitfield of size "+ bitfieldMessage.messagePayload.length);
				oos.writeObject(bitfieldMessage);
			} else {

				//					peerProcess.logger.println("Bitfield Expected");

			}

			//				peerProcess.logger.print("Going to sent message");
			//				ActualMessage testChunk = new ActualMessage(makeChunk(16),16);
			//				oos.writeObject(testChunk);


			if (complete) {
				chokeTimer.cancel();
				chokeTimer.purge();
				optChokeTimer.cancel();	
				optChokeTimer.purge();
				return;
			}

			while(!complete) {

				if (complete) {
					chokeTimer.cancel();
					chokeTimer.purge();
					optChokeTimer.cancel();	
					optChokeTimer.purge();
					return;
				}

				//Recving message
				ActualMessage messageRcvd = (ActualMessage)ois.readObject();
				//					peerProcess.logger.println("Received message from "+hmRecvd.peerID+" type "+messageRcvd.messageType);
				completeAndExit:
					switch(messageRcvd.messageType) {
					case 0:
						//choke
						//set choke flag
						choked = true;
						peerProcess.logger.print(peerProcess.myID+" is choked by "+hmRecvd.peerID);
						break;

					case 1:
						synchronized(this){
							//recvd unchoke - send a request for any piece you dont have, set choke flag to flase
							peerProcess.logger.println(peerProcess.myID+" is unchoked by "+hmRecvd.peerID);
							choked = false;

							//selecting randomly a chunk number from the dont have list only if any chunk is missing.
							ArrayList<Integer> dontHaveChunkList_temp= new ArrayList<Integer>(dontHaveChunkList);
							if(dontHaveChunkList_temp.size()>0) {
								Random rand = new Random();
								while(dontHaveChunkList_temp.size()>0) {
									int chunkRequestedFor = dontHaveChunkList_temp.get(rand.nextInt(dontHaveChunkList_temp.size()));
									if(listOfPeersandChunks[hisRank-1][chunkRequestedFor]==1) {
										oos.writeObject(new ActualMessage("request",chunkRequestedFor));
										//										peerProcess.logger.println("Received unchoke and sending request to "+hmRecvd.peerID+" for chunk: "+chunkRequestedFor);
										break;
									}

									//							peerProcess.logger.println("Attempting to remove element "+chunkRequestedFor+"having index"+dontHaveChunkList_temp.indexOf(chunkRequestedFor));
									//							peerProcess.logger.println("the don't have chunk list is now"+dontHaveChunkList.toString());
									dontHaveChunkList_temp.remove(dontHaveChunkList_temp.indexOf(chunkRequestedFor));

								}
							}
							//					peerProcess.logger.print("After copy and remove at top : "+dontHaveChunkList.toString());
							break;
						}

					case 2:
						synchronized(this){
							//recvd interested
							peerProcess.logger.println(peerProcess.myID+" received the interested message from "+hmRecvd.peerID);
							//Update the interested list
							interestedList.add(getRank(hmRecvd.peerID+"")-1);
							//						peerProcess.logger.println("Interested List is "+interestedList.toString());
							interestedList=removeDuplicates(interestedList);
							//							peerProcess.logger.println("Interested List without dups is "+interestedList.toString());
							//send unchoke after selecting neighbour from preferred Neighbor

							if((unchokedList.size()<peerProcess.nofPreferredNeighbour) && !unchokedList.contains(getRank(hmRecvd.peerID)-1)) {
								unchokedList.add(getRank(hmRecvd.peerID)-1);	
								oos.writeObject(new ActualMessage("unchoke"));
								//								peerProcess.logger.println("Sending unchoke to "+hmRecvd.peerID);
							}else{
								chokedList.add((getRank(hmRecvd.peerID)-1));
								oos.writeObject(new ActualMessage("choke"));
								//								peerProcess.logger.println("Sending choke to "+hmRecvd.peerID);
							}

							break;

						}
					case 3:
						synchronized(this){
							//recvd not interested
							peerProcess.logger.println(peerProcess.myID+ " received a not interested message from "+hmRecvd.peerID);

							//Remove if in interested list
							interestedList.remove((Object)(getRank(hmRecvd.peerID)-1));
							if(unchokedList.contains(getRank(hmRecvd.peerID)-1)){
								unchokedList.remove((Object)(getRank(hmRecvd.peerID)-1));
							}
							else {
								chokedList.add((getRank(hmRecvd.peerID)-1));
							}

							break;

						}
					case 4:
						synchronized(this){
							//recvd have message
							//update your list
							int chunkIndex = messageRcvd.getChunkid();
							listOfPeersandChunks[hisRank-1][chunkIndex]=1;
							peerProcess.logger.print(peerProcess.myID+ " received the have message from "+hmRecvd.peerID);
							//send interested if you want that piece else not interested

							//					peerProcess.logger.print("DontHaveChunk list after recieving have is "+dontHaveChunkList.toString());
							if(dontHaveChunkList.contains(chunkIndex)) {
								ActualMessage interested = new ActualMessage("interested");
								oos.writeObject(interested);
								//								peerProcess.logger.print("Sent an interested message to"+hmRecvd.peerID+"for chunkid "+chunkIndex);
							} else {
								ActualMessage notinterested = new ActualMessage("notinterested");
								oos.writeObject(notinterested);
								//								peerProcess.logger.print("Sent a NOT interested message to"+hmRecvd.peerID+"for chunkid "+chunkIndex);
							}
							break;
						}

					case 5:
						synchronized(this){
							//Recving bitfield message
							//							peerProcess.logger.print("Inside bitfield - switch case.--------");
							//					ActualMessage bitfieldMessage = (ActualMessage) ois.readObject();
							//					ActualMessage bitfieldMessage = (ActualMessage) ois.readObject();
							//Recving bitfield message
							//							peerProcess.logger.print(peerProcess.myID + " recieved bitfield message of type "+messageRcvd.messageType +" size "+ messageRcvd.messagePayload.length + " from " +hmRecvd.peerID);
							BitSet myRecvBits = new BitSet(peerProcess.nofPieces);
							myRecvBits = messageRcvd.toBitSet(messageRcvd.messagePayload);

							String toPrint2 = new String();
							for(int[] a:listOfPeersandChunks){
								for(int b:a){
									toPrint2+=b+",";
								}
								toPrint2+="\n";
							}
							//					peerProcess.logger.print("\n Peer and Chunk Info 1: \n"+toPrint2);	

							//Updating the list of the chunks the other peer has
							if(!myRecvBits.isEmpty())
							{
								hisRank = getRank(Integer.toString(hmRecvd.peerID));
								for(int x=myRecvBits.nextSetBit(0); x>=0; x=myRecvBits.nextSetBit(x+1)) {
									//							peerProcess.logger.print(" x "+x);	
									listOfPeersandChunks[hisRank-1][x] =1;
								}
							}

							String toPrint = new String();
							for(int[] a:listOfPeersandChunks){
								for(int b:a){
									toPrint+=b+",";
								}
								toPrint+="\n";
							}
							//					peerProcess.logger.print("\n Peer and Chunk Info: \n"+toPrint);				

							//updating your dont have list
							for(int j=0;j<peerProcess.nofPieces;j++) {
								if(listOfPeersandChunks[peerProcess.myRank-1][j]==0) {
									dontHaveChunkList.add(j);
								}

							}

							boolean sentInterested = false;
							//send interested message if he has any piece you dont and only if you need atleast one piece
							if(dontHaveChunkList.size()>0) {
								Random rand2 = new Random();
								//selecting randomly from the dont have list.
								while(true) {
									//									peerProcess.logger.println("Got bitfield, dontHAveChunkList is "+dontHaveChunkList.toString()+" with size"+ dontHaveChunkList.size());
									if(listOfPeersandChunks[hisRank-1][dontHaveChunkList.get(rand2.nextInt(dontHaveChunkList.size()))]==1) {
										ActualMessage interested = new ActualMessage("interested");
										oos.writeObject(interested);
										sentInterested = true;
										break;
									}
								}

							}

							if(!sentInterested) {
								ActualMessage interested = new ActualMessage("notinterested");
								oos.writeObject(interested);
							}

							break;
						}

					case 6:
						synchronized(this){
							//recvd request
							int reqIndex = messageRcvd.getChunkid();
							//							peerProcess.logger.print(hmRecvd.peerID+" requested for chunk "+reqIndex);
							//if unchoked send piece
							if(optimisticUnchokeList.contains(getRank(hmRecvd.peerID)-1)||true||unchokedList.contains((getRank(hmRecvd.peerID)-1))) {
								if(peerProcess.haveFile==1) {
									oos.writeObject(new ActualMessage(makeChunk(reqIndex),reqIndex));
									//									peerProcess.logger.print("Making and sending "+hmRecvd.peerID+" the chunk "+reqIndex);
								}
								else {
									//									peerProcess.logger.print("case 6: else The listOfPeersandChunks["+ (peerProcess.myRank-1) +"]["+reqIndex+"]="+listOfPeersandChunks[peerProcess.myRank-1][reqIndex]);
									if(listOfPeersandChunks[peerProcess.myRank-1][reqIndex]==1){
										File chunkFile = new File(fileDirectory+reqIndex+".part");
										FileInputStream partInput = new FileInputStream(chunkFile);
										//										peerProcess.logger.println("Length of chunk "+chunkFile.length()+"in ints "+(int)chunkFile.length() );
										byte[] chunkb = new byte[(int)chunkFile.length()];
										//										peerProcess.logger.println("Length of byte array"+ chunkb.length);
										partInput.read(chunkb, 0, (int)chunkFile.length());
										oos.writeObject(new ActualMessage(chunkb,reqIndex));
										//										peerProcess.logger.print("Forwarding "+hmRecvd.peerID+" the chunk "+reqIndex);
									}
								}

							}
							else {
								//								peerProcess.logger.print(hmRecvd.peerID+" is choked and does not receive "+reqIndex + " from me.");
							}

							break;
						}
					case 7:
						synchronized(this){
							// recv piece 
							chunkNumber = messageRcvd.getChunkid();
							makePartFile(messageRcvd.getPayload(),chunkNumber);

							peerProcess.logger.print(peerProcess.myID+" has downloaded the piece "+chunkNumber+" from " +hmRecvd.peerID);
							//							peerProcess.logger.print("Dont have list: "+dontHaveChunkList.toString());

							//Update download stats
							//					downloadPieces.add(index, element
							downloadPieces[hisRank-1]++;
							//update matrix and dont have list
							if (dontHaveChunkList.indexOf(chunkNumber)>=0){
								dontHaveChunkList.remove(dontHaveChunkList.indexOf(chunkNumber));
							}
							listOfPeersandChunks[peerProcess.myRank-1][chunkNumber]=1;
							//							peerProcess.logger.print("case 7 :Updated the listOfPeersandChunks["+ (peerProcess.myRank-1) +"]["+chunkNumber+"]="+listOfPeersandChunks[peerProcess.myRank-1][chunkNumber]);


							//after recieving broadcast have
							broadcastHave(chunkNumber);
							haveChunk = true;  //initiate the server sequence even if it has one chunk and only if not already a server

							//put a logic to see if all pieces are complete, if so broadcast bye and die
							if(dontHaveChunkList.isEmpty()&&isComplete()) {
								peerIDList.remove((Object) hmRecvd.peerID);
								mergeChunks();
								peerProcess.logger.print(peerProcess.myID+" has downloaded the complete file. ");
								sleep(10000);
								broadcastBye();



								break;


							}

							//check if unchoked and then request for next piece

							if(!choked&&!complete) {
								//selecting randomly a chunk number from the dont have list only if any chunk is missing.
								ArrayList<Integer> dontHaveChunkList_temp2= new ArrayList<Integer>(dontHaveChunkList);
								if(dontHaveChunkList_temp2.size()>0) {
									Random rand = new Random();
									while(true) {
										if(dontHaveChunkList_temp2.size()>0) {	
											//											peerProcess.logger.print("Processing While in choked");
											int chunkRequestedFor = dontHaveChunkList_temp2.get(rand.nextInt(dontHaveChunkList_temp2.size()));
											if(listOfPeersandChunks[hisRank-1][chunkRequestedFor]==1) {
												oos.writeObject(new ActualMessage("request",chunkRequestedFor));
												//												peerProcess.logger.println("Requesting again to "+hmRecvd.peerID+" for chunk: "+chunkRequestedFor);
												break;
											}
											else {
												//												peerProcess.logger.println("Not Requesting because "+hmRecvd.peerID+" dooes not have  chunk: "+chunkRequestedFor);
											}
											dontHaveChunkList_temp2.remove(dontHaveChunkList_temp2.indexOf(chunkRequestedFor));
										}
									}
								}
								//						peerProcess.logger.print("After copy and remove : "+dontHaveChunkList.toString());
							}


							break;

						}
					case 8:
						//						peerProcess.logger.println("Received a BYE message from "+hmRecvd.peerID);
						//						peerProcess.logger.println("Shutting down this thread ");
						byeCount++;

						if((peerProcess.haveFile==1)&&(byeCount>=(peerProcess.nofPeers-1))){
							//							peerProcess.logger.println("I was the owner of the file. Everybody downloaded. So I am exiting as well.");
							complete = true;


						}

						//make his matrix row zero too stop requesting him.
						for(int d=0;d<peerProcess.nofPieces;d++) {
							listOfPeersandChunks[hisRank-1][d]=0;
						}

						//removing him from unchoke list
						peerIDList.remove((Object) hmRecvd.peerID);
						unchokedList.remove((Object)(hisRank-1));

						interestedList.remove((Object)(hisRank-1));

						preferredNeighbors.remove((Object)(hisRank-1));


						return;


					}//end switch case


				//					mergeChunks();
				//					peerProcess.logger.print("Merged chunks");

			}//end while
		} catch (UnknownHostException e) {
			peerProcess.logger.print(e);

			e.printStackTrace();
		} catch (FileNotFoundException e) {
			peerProcess.logger.print(e);
			e.printStackTrace();
		} catch (IOException e) {
			peerProcess.logger.print(e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			peerProcess.logger.print(e);
			e.printStackTrace();
		}finally{
			try {



				oos.close();
				ois.close();

				for(Thread t: threads){
					t.join();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}






	}

	public boolean isComplete() {

		for(int i=0;i<peerProcess.nofPieces;i++) {
			if(listOfPeersandChunks[peerProcess.myRank-1][i]!=1) {
				return false;
			}

		}
		return true;

	}

	public byte[] makeChunk(int chunkNo) {
		byte[] outChunks = null;
		if(peerProcess.haveFile ==1) {
			try {

				peerProcess.theFile = new File(peerProcess.fileName);
				assert (!peerProcess.theFile.exists());
				RandomAccessFile ramFile = new RandomAccessFile(peerProcess.theFile, "r");
				ramFile.seek((long)peerProcess.pieceSize*chunkNo);
				//				peerProcess.logger.print("Chunk number : "+chunkNo+" Now at offset: " +ramFile.getFilePointer());
				if(peerProcess.pieceSize*(chunkNo+1)>ramFile.length()){
					outChunks = new byte[(int) (peerProcess.pieceSize*(chunkNo+1)-ramFile.length())];
					ramFile.read(outChunks, 0, (int) (peerProcess.pieceSize*(chunkNo+1)-ramFile.length()));
					//					peerProcess.logger.print("in IF :"+chunkNo+" "+(peerProcess.pieceSize*(chunkNo+1)-ramFile.length()));
				} else {
					outChunks = new byte[peerProcess.pieceSize];
					ramFile.read(outChunks, 0, peerProcess.pieceSize);
				}
				//			peerProcess.logger.print("\nRead \n"+new String(outChunks)+"\n");
				ramFile.close();	
				//				peerProcess.logger.println("I have the file. SPLIT it into "+outChunks.length+" chunks ");


			} catch (Exception e) {

				peerProcess.logger.println(e.toString());
			}


		}
		//		peerProcess.logger.println("Chunk size is "+outChunks.length);
		return outChunks;
	}

	public void makePartFile(byte[] inputChunk, int chunkNumber) {
		String partName = chunkNumber + ".part";
		File outputFile = new File(partName);
		try {
			new File(fileDirectory).mkdir();
			FileOutputStream fos = new FileOutputStream(fileDirectory+outputFile);
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

			FileOutputStream opfos = new FileOutputStream(fileDirectory+outputFile,false);
			for(int j=0;j<peerProcess.nofPieces;j++) {

				String partNameHere = j + ".part";
				//				peerProcess.logger.print("Merging piece :"+partNameHere+" /"+peerProcess.nofPieces);


				File partFile = new File(fileDirectory+partNameHere);
				FileInputStream pffis = new FileInputStream(partFile);
				//				peerProcess.logger.println("Length of partfile "+partFile.length());
				byte[] fb = new byte[(int)partFile.length()];
				//				peerProcess.logger.println("Length of byte array"+ fb.length);
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

	void broadcastHave(int chunkIndex){
		//		peerProcess.logger.print("consolidated_oos.length"+consolidated_oos.length);
		for(int i =0; i<consolidated_oos.length; i++){

			if(null!=consolidated_oos[i]&&i!=(peerProcess.myRank-1)&&i!=(hisRank-1)){
				//				peerProcess.logger.print("Sending have message to "+(i+1));
				consolidated_oos[i].writeObject(new ActualMessage("have",chunkIndex));
			}
		}
		//		peerProcess.logger.print("Broadcasted the have message");
	}

	void broadcastBye(){

		for(int i =0; i<consolidated_oos.length; i++){

			if(null!=consolidated_oos[i]&&i!=(peerProcess.myRank-1)){
				//				peerProcess.logger.print("Sending bye message to "+(i+1));
				consolidated_oos[i].writeObject(new ActualMessage("bye"));
			}
		}
		//		peerProcess.logger.print("Broadcasted the Bye message");
	}

}