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
					String temp = (String) ois.readObject();
					System.out.println("Got:"+temp);
				}else{
					System.out.println("Tried to send");
					Object obj = q.take();
					System.out.println("Object is "+obj.toString());
					oos.writeObject(obj);
					oos.flush();

				}
				sleep(1000);
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
		}
	}
	
   public boolean writeObject(Object obj){
	   try {
		q.put(obj);
	} catch (InterruptedException e) {
		peerProcess.logger.print(e.getMessage());
		return false;
	}
	   return true;
   }
}