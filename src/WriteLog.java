import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

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
		synchronized(this){
			out.flush();
			out.println(TimeGen.now()+" "+peerProcess.myID+"::"+msg);
			out.flush();
		}
	}

	public void print(Throwable t){
		synchronized(this){

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			t.printStackTrace(pw);
			pw.flush();
			sw.flush();
			print(sw.toString());
		}
	}

	public void println(String msg){
		print(msg);
	}
	public void close(){
		out.println(" ");
		out.close();}

}
