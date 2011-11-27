import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class ParallelStream extends Thread {

	private ObjectInputStream ois;
	private ObjectOutputStream oos;
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
		while(true){
			//		synchronized(this){	
			try{
				if(isInput){
					synchronized(this){
						q.put(ois.readObject());
						peerProcess.logger.println("Read Object from ois and inserted, q(with object is now)"+q.toString());//+obj.toString());
					}
				}else{
					//					Object obj = q.take();
					//					peerProcess.logger.println("Wrote Object:"+obj.toString());
					oos.writeObject(q.take());
					oos.flush();

				}
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
				q.put(obj);
				//				peerProcess.logger.println("writeObject: inserted into q");
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
				peerProcess.logger.println("readObject invoked with q (waiting to pop):"+q.toString());
				Object obj=q.take();
				peerProcess.logger.println("readObject:popped "+obj.toString()+" from q :"+q.toString());
				return obj;
			} catch (InterruptedException e) {
				peerProcess.logger.print(e.getMessage());
				return null;
			}
		}
		return null;
	}
}