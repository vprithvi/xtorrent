import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

class WriteLog{
	FileWriter outFile;
	PrintWriter out;
	String client_id;
	boolean isReader;

	public	WriteLog(String client_id){
		this.client_id=client_id;
		try {
			outFile = new FileWriter("log_peer_"+client_id+".log");
		} catch (IOException e) {
			e.printStackTrace();
		}
		out = new PrintWriter(outFile);
	}

	public void print(String msg){
		if(msg.contains("onnect")){
			msg = msg.toUpperCase();
			msg = ""+msg+"";
		}
		out.flush();
		out.println(TimeGen.now()+" "+peerProcess.myID+"::"+msg);
		out.flush();
	}

	public void println(String msg){
//		out.flush();
//		out.println(TimeGen.now()+":"+msg);
//		out.flush();
		print(msg);
	}
	public void close(){
		out.println(" ");
		out.close();}

}
