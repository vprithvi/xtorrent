import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class ParallelStream extends Thread {

	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	BlockingQueue<Object> q = new LinkedBlockingQueue<Object>();
//	static private List<Thread> threads = new ArrayList<Thread>();
	boolean isInput = false;
	boolean isClose =false;

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
			if(isClose){
				return;
			}

			try{
				if(isInput){
					synchronized(ois){

						q.put(ois.readObject());


					}
				}else{
					synchronized (oos) {
						oos.flush();
						oos.writeObject(q.take());
						oos.flush();
					}
				}
			}catch(Exception e){
				peerProcess.logger.println(e.toString());
			}
		}

	}

	public boolean writeObject(Object obj){

		if (!isInput) {
			try {
				q.put(obj);

			} catch (InterruptedException e) {
				peerProcess.logger.print(e.toString());
				return false;
			}
			return true;
		} else {
			assert (!isInput);
			return false;

		}
	}

	public Object readObject(){
		if(isInput){
			try {
				Object obj=q.take();
				return obj;
			} catch (InterruptedException e) {
				peerProcess.logger.print(e.toString());

				return null;
			}
		}
		assert (isInput);
		return null;
	}

	public void close() throws IOException{
		if(isInput){
			isClose = true;
			ois.close();
		}else{
			ois.close();
		}

	}
}