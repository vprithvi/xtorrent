import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class ParallelStream extends Thread {

	private static ObjectInputStream ois;
	private static ObjectOutputStream oos;
	BlockingQueue<Object> q = new LinkedBlockingQueue<Object>();

	boolean isInput = false;

	public ParallelStream(ObjectInputStream o){
		ois=o;	
		isInput = true;
		this.start();
	}

	public ParallelStream(ObjectOutputStream o){
		oos=o;
		this.start();
	}


	public void run(){
//		while(true){
			synchronized(this){	
				try{
					if(isInput){
//						Object obj = ois.readObject();
						q.put(ois.readObject());
//	                    Object obj = q.					
						peerProcess.logger.println("Read Object:");//+obj.toString());
					}else{
//						peerProcess.logger.println("Tried to send");
						Object obj = q.take();
						peerProcess.logger.println("Wrote Object:"+obj.toString());
						oos.writeObject(obj);
						oos.flush();

					}
					peerProcess.logger.println("looping"); 	
				}catch(Exception e){
					peerProcess.logger.println(e.getMessage());
				}
			}
//		}
	}

	public boolean writeObject(Object obj){
//		peerProcess.logger.println("is input in writeobject"+isInput);
		if (!isInput) {
			try {
//				peerProcess.logger.println("is not input");
				q.put(obj);
				peerProcess.logger.println("writeObject: inserted into q");
			} catch (InterruptedException e) {
				peerProcess.logger.print(e.getMessage());
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	public Object readObject(){
		if(isInput){
			try {
				peerProcess.logger.println("readObject:read from q");
				return q.take();
			} catch (InterruptedException e) {
				peerProcess.logger.print(e.getMessage());
				return null;
			}
		}
		return null;
	}
}