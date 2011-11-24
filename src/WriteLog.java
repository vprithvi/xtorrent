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
			outFile = new FileWriter(client_id+".log");
		} catch (IOException e) {
			e.printStackTrace();
		}
		out = new PrintWriter(outFile);
	}

	public void print(String msg){
		out.flush();
		out.println(msg);
	}

	public void println(String msg){
		out.flush();
		out.println(msg);
	}
	public void close(){
		out.println(" ");
		out.close();}

}
