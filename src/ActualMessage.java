import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.BitSet;


public class ActualMessage implements Serializable {


	private static final long serialVersionUID = 3545119718998974040L;
	int length;
	int messageType;
	byte[] messagePayload;

	//Constructor for choke,unchoke, interested, not interested
		ActualMessage(String mType) {
			if(mType=="choke") {
				messageType = 0;
			}
			else if(mType=="unchoke") {
				messageType = 1;
			}
			else if(mType=="interested") {
				messageType = 2;
			}
			else if(mType=="notinterested"){ 
				messageType = 3;
			}
			else {
				messageType=8;
			}
			
			length = 0;
		}
		
		//Constructor for have, request
		ActualMessage(String mType,int index) {
			if(mType=="have") {
				messageType = 4;
			}
			else { //request
				messageType = 6;
			}
			byte[] chunkIndex = ByteBuffer.allocate(4).putInt(index).array();
			messagePayload = new byte[chunkIndex.length];
			System.arraycopy (chunkIndex,0,messagePayload,0, chunkIndex.length);
			
			
			length = 0;
		}
	
	//Constructor for bitfield message
	ActualMessage(int pieces) {
		messageType = 5;
		BitSet bitfield = new BitSet(pieces);
		for(int i=0;i<pieces;i++) {
		bitfield.set(i);
		}
		 byte[] abcd= toByteArray(bitfield).clone();
		 messagePayload = abcd.clone();
		length = messagePayload.length;
		
		
	}

	//Constructor for piece messages
	ActualMessage(byte[] myChunk, int chunkid) {
		byte[] chunkid_b = ByteBuffer.allocate(4).putInt(chunkid).array();
		
		assert (myChunk.length > 0);
		assert (chunkid_b.length == 4);
		messageType = 7;
		messagePayload = new byte[myChunk.length+chunkid_b.length];
		
		System.arraycopy (chunkid_b,0,messagePayload,0, chunkid_b.length);
		System.arraycopy (myChunk,0,messagePayload,chunkid_b.length, myChunk.length);
		length = messagePayload.length;
	}


	public int getChunkid(){
	  ByteBuffer chunkid_b = ByteBuffer.wrap(messagePayload);
	  chunkid_b.position(0);
	  return chunkid_b.getInt();
	}
	
	public byte[] getPayload(){
	  byte[] payload = new byte[messagePayload.length-4];
	  System.arraycopy(messagePayload, 4, payload, 0, messagePayload.length-4);
	  return payload;
	}

	public BitSet toBitSet(byte[] bytes) {
		BitSet bits = new BitSet();
		for(int i=0;i<bytes.length*8;i++) {
			if((bytes[bytes.length-i/8-1]&(1<<(i%8))) > 0){
				bits.set(i);
			}
		}
		return bits;
	}

	public byte[] toByteArray (BitSet bits) {
		byte[] bytes = new byte[bits.length()/8+1];
		for(int i=0;i<bits.length();i++) {
			if(bits.get(i)) {
				bytes[bytes.length-i/8-1] |= 1<<(i%8);
			}
		}
		return bytes;
	}
}
