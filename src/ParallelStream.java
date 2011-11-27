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
		while(true){
			try{
				if(isInput){
					Object obj = ois.readObject();
					System.out.println("Got:"+(String)obj);
					q.put(obj);
				}else{
					System.out.println("Tried to send");
					Object obj = q.take();
					System.out.println("Object is "+obj.toString());
					oos.writeObject(obj);
					oos.flush();

				}
				sleep(10);
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
		}
	}

	public boolean writeObject(Object obj){
		if (!isInput) {
			try {
				q.put(obj);
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
				return q.take();
			} catch (InterruptedException e) {
				peerProcess.logger.print(e.getMessage());
				return null;
			}
		}
		return null;
	}
}