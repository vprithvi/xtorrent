
public class ActualMessage {
	
	int length;
	Byte messageType;
	Byte messagePayload;
	
	ActualMessage(int l,Byte mT, Byte mP) {
		length = l;
		messageType = new Byte(mT); 
		messagePayload = new Byte(mP); // Single chunk in byte
		
	}

}
