/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
    public static int mechanicID = 250;
    public static int requestID = 30000;
    public static int getRequestID(){
		requestID++;
		return requestID;
	}
	public static int getMechanicID(){
		mechanicID++;
		return mechanicID;
	}
	
	public static boolean isInteger(String s) {
      boolean isValidInteger = false;
      try
      {
         Integer.parseInt(s);
 
         // s is a valid integer
 
         isValidInteger = true;
      }
      catch (NumberFormatException ex)
      {
         // s is not an integer
      }
 
      return isValidInteger;
   }
	
	public static void AddCustomer(MechanicShop esql){//1
	    try{

		String fname = "";
		String lname = "";
		String phone = "";
		String address = "";
		int customerID = 601;

		System.out.print("\nEnter first name (MAX 32 CHAR): ");
		String input = in.readLine();
		while(input.length() > 32) {
		    System.out.print("\nInvalid entry. Enter first name (MAX 32 CHAR): ");
		    input = in.readLine();
		}
		fname += input;

		System.out.print("\nEnter last name (MAX 32 CHAR): ");
		input = in.readLine();
		while(input.length() > 32) {
		    System.out.print("\nInvalid entry. Enter last name (MAX 32 CHAR): ");
		    input = in.readLine();
		}
		lname += input;

		System.out.print("\nEnter phone number (MAX 13 DIGITS): ");
		input = in.readLine();
		while(input.length() > 13) {
		    System.out.print("\nInvalid entry. Enter phone number (MAX 13 DIGITS): ");
		    input = in.readLine();
		}
		phone += input;

		System.out.print("\nEnter address (MAX 256 CHAR): ");
		input = in.readLine();
		while(input.length() > 256) {
		    System.out.print("\nInvalid entry. Enter address (MAX 256 CHAR): ");
		    input = in.readLine();
		}
		address += input;

		//int id = customerID;

		String query = "INSERT INTO Customer(id, fname, lname, phone, address) VALUES(" + customerID + ", '" + fname + "', '" + lname + "', '" + phone + "', '" + address + "');"; 

		esql.executeUpdate(query);
		

	    }catch(Exception e){
		System.err.println(e.getMessage());
	    }
	}
	
	public static void AddMechanic(MechanicShop esql){//2
		try{
			System.out.print("Enter mechanic first name (MAX 32 CHAR): ");
			String input = in.readLine();
			while(input.length() > 32 || input.length() == 0) {
				System.out.print("Invalid entry. Enter mechanic first name (MAX 32 CHAR): ");
				input = in.readLine();
			}
			String fname = input;
			
			System.out.print("Enter mechanic last name (MAX 32 CHAR): ");
			input = in.readLine();
			while(input.length() > 32 || input.length() == 0) {
				System.out.print("Invalid entry. Enter mechanic last name (MAX 32 CHAR): ");
				input = in.readLine();
			}
			String lname = input;
			
			System.out.print("Enter experience in years: ");
			input = in.readLine();
			while(input.length() == 0 || !isInteger(input)) {
				System.out.print("Invalid entry. Enter experience in years: ");
				input = in.readLine();
			}
			String years = input;
			
			String query = "INSERT INTO Mechanic Values (";
			query += getMechanicID();
			query += ", '";
			query += fname;
			query += "', '";
			query += lname;
			query += "', '";
			query += years;
			query += "')";
			
			esql.executeUpdate(query);
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void AddCar(MechanicShop esql){//3
		
	}
	
	public static void InsertServiceRequest(MechanicShop esql){//4
		try{
			System.out.print("Enter last name (MAX 32 CHAR): ");
			String input = in.readLine();
			while(input.length() > 32 || input.length() == 0) {
				System.out.print("Invalid entry. Enter last name (MAX 32 CHAR): ");
				input = in.readLine();
			}
			String lname = input;
			
			String query = "Select id, fname, lname from Customer where lname='";
			query += lname;
			query += "'";
			
			esql.executeQueryAndPrintResult(query);
			
			System.out.print("Would you like to add a new customer?(y/n)");
			input = in.readLine();
			if(input.equals("y")){
				System.out.print("Adding customer.\n");
				AddCustomer(esql);
			}
			
			System.out.print("Enter id of customer you wish to initiate a service request for: ");
			input = in.readLine();
			while(!isInteger(input) || input.length() == 0) {
				System.out.print("Invalid entry. Enter id of customer you wish to initiate a service request for: ");
				input = in.readLine();
			}
			String cid = input;
			query = "Select car_vin from Owns where customer_id=" + cid;
			
			esql.executeQueryAndPrintResult(query);
			
			System.out.print("Would you like to initiate a service request for one of these cars?(y/n)");
			input = in.readLine();
			String choice = input;
			if(choice.equals("y")){
				System.out.print("Enter VIN: ");
				input = in.readLine();
				while(input.length() != 16) {
					System.out.print("Invalid entry. VIN is 11-17 characters (pre 1981) or 17 characters (post 1981). But apparently for this project it's exactly 16 characters. Enter VIN: ");
					input = in.readLine();
				}
				String car_vin = input;
				String timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(Calendar.getInstance().getTime());
				System.out.print("Using timestamp " + timeStamp + "\n");
				
				System.out.print("Enter odometer: ");
				input = in.readLine();
				while(!isInteger(input) || input.length() == 0) {
					System.out.print("Invalid entry. Enter odometer: ");
					input = in.readLine();
				}
				String odometer = input;
				
				System.out.print("Enter complaint: ");
				input = in.readLine();
				while(input.length() == 0) {
					System.out.print("Invalid entry. Enter complaint: ");
					input = in.readLine();
				}
				String complaint = input;
				
				query = "Insert into Service_Request values(";
				query += getRequestID();
				query += ", '";
				query += cid;
				query += "', '";
				query += car_vin;
				query += "', '";
				query += timeStamp;
				query += "', '";
				query += odometer;
				query += "', '";
				query += complaint;
				query += "'";
			}
			else{
				System.out.print("Inserting new car.\n");
				AddCar(esql);
			}
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		try{			
			String query = "";
			query += "SELECT cu.fname, cu.lname, c.date, c.comment, c.bill ";
			query += "FROM Closed_Request c, Customer cu, Service_Request r ";	
			query += "WHERE c.bill < 100 AND c.rid=r.rid AND r.customer_id=cu.id;";		
			esql.executeQueryAndPrintResult(query);
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		try{			
			String query = "";
			query += "SELECT c.make, c.model, c.year, s.odometer ";
			query += "FROM Car c, Service_Request s ";	
			query += "WHERE c.year < 1995 AND c.vin=s.car_vin AND odometer < 50000 ORDER BY c.year;";		
			esql.executeQueryAndPrintResult(query);
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		//
		
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//10
		try{
		    String query = "";
		    query += "SELECT c.fname , c.lname, tb ";
		    query += "FROM Customer c, ";
		    query += "(SELECT s.customer_id, SUM(cl.bill) tb FROM Service_Request s, Closed_Request cl WHERE s.rid=cl.rid GROUP BY s.customer_id) te ";
		    query += "WHERE c.id=te.customer_id ORDER BY te.tb DESC;";
			esql.executeQueryAndPrintResult(query);
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
}
