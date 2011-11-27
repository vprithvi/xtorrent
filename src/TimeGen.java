import java.util.Calendar;
import java.text.SimpleDateFormat;

public class TimeGen {
	public static final String DATE_FORMAT = "[yyyy-MM-dd HH:mm:ss,SSS]";

	public static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateF = new SimpleDateFormat(DATE_FORMAT);
		return dateF.format(cal.getTime());

	}

	public static void  main(String arg[]) {
		System.out.println(TimeGen.now());
	}
}

