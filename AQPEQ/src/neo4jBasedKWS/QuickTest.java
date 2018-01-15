package neo4jBasedKWS;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

public class QuickTest {

	public static void main(String[] args) throws Exception {

		Environment myDbEnvironment = null;
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		myDbEnvironment = new Environment(new File("/export/dbEnv"), envConfig);
		

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		// String formattedDate = formatter.format("2010-06-15 23:55:57");

		System.out.println(formatter.parse("2010-06-15T23:55:57"));

		String tt = "xin123zhang456";

		tt = tt.replaceAll("[0-9]+", " ").replaceAll("\\s+", " ").trim();
		System.out.println("tt:" + tt + ".");

		MyObject object = new MyObject();
		HashMap<String, MyObject> map = new HashMap<String, MyObject>();

		map.put("a", object);

		System.out.println("before:" + map.get("a"));
		testMehtod(map.get("a"));
		System.out.println("after:" + map.get("a"));

	}

	public static void testMehtod(MyObject obj) {
		obj.x++;
	}

	public static void testMehtod(Integer myInt) {
		myInt++;
	}

}

class MyObject {
	public int x = 0;

}
