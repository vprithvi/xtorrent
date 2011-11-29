import java.io.Serializable;
import java.util.BitSet;


public class ActualMessage implements Serializable {


	private static final long serialVersionUID = 3545119718998974040L;
	int length;
	int messageType;
	byte[] messagePayload;


	//Constructor for bitfield message
	ActualMessage(int pieces) {
		messageType = 5;
		BitSet bitfield = new BitSet(pieces);
		messagePayload = new byte[bitfield.length()/8];
		messagePayload = toByteArray(bitfield);
		length = messagePayload.length;
	}

	//Constructor for messages with chunk
	ActualMessage(byte[] myChunk) {

		messageType = 7;
		messagePayload = myChunk.clone();

		length = messagePayload.length;
		//		peerProcess.logger.println("Actual message payload content is \n\n\n"+new String(messagePayload)+"\n\n");
	}



	/*	ActualMessage(int l,Byte mT, Byte mP) {
		length = l;
		messageType = new Byte(mT); 
		messagePayload = new Byte(mP); // Single chunk in byte
		bitfield = new BitSet(l);
		bitfield.set(1,true);
	}*/

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
