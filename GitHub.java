import java.io.*;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class GitHub
{
    private String url;
    private GregorianCalendar date;
    private Connection dbCon;
    private PreparedStatement pstmt;
    
    GitHub(String url,GregorianCalendar date){
	this.url=url;
	this.date=date;
	this.dbCon=null;
	this.pstmt=null;
    }

    void getCommit(){
	init();
	String response=get(url);
	String[] repos=fromListToArray(response);
	commits(repos);
	close();
    }

    void getLimits(){
	System.out.println(gets("https://api.github.com/rate_limit"));
    }

    private void init(){
	openDB();

	try{
	    this.pstmt=dbCon.prepareStatement("INSERT INTO Commits "+
		    "VALUES (?,?,?,?,?,?)");
	}
	catch(SQLException ex){
	    System.err.println("SQLException: "+ex.getMessage()+"\nDatabase access error occurred");
	    System.exit(1);
	}
    }

    private void close(){
	try{
	    if(pstmt!=null){
		pstmt.close(); pstmt=null;}

	    if(dbCon!=null){
		dbCon.close(); dbCon=null;}
	}
	catch(SQLException ex){
	    System.err.println("SQLException: "+ex.getMessage());
	}
    }

    //Gets https targetURL response body
    //Prints to output informations about sent and received HTTP header and response status
    private String get(String targetURL)
    {
	String response=null;

	HttpsURLConnection connection = null;  
	try {
	    //Create connection
	    URL url = new URL(targetURL);
	    connection = (HttpsURLConnection)url.openConnection();
	    connection.setRequestMethod("GET");
	    connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
	    connection.setRequestProperty("User-Agent", "Firefox");
	    connection.setUseCaches(false);



	    System.out.println("\nSending 'GET' request to URL : " + url);
	    connection.connect();
	    int responseCode = connection.getResponseCode();
	    System.out.println("getRequestProperty(\"User-Agent\"): "+connection.getRequestProperty("User-Agent"));
	    System.out.println("getRequestProperty(\"Host\"): "+connection.getRequestProperty("Host"));
	    System.out.println("getRequestProperty(\"Accept\"): "+connection.getRequestProperty("Accept"));
	    System.out.println("getRequestProperty(\"Connection\"): "+connection.getRequestProperty("Connection"));
	    System.out.println("getRequestProperty(\"Cache-Control\"): "+connection.getRequestProperty("cache-control"));
	    System.out.println("Response Code : " + responseCode);

 
	    
	    System.out.println("\nPrinting Response Header...\n");
	    Map<String, List<String>> map = connection.getHeaderFields();
	    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
	        System.out.println( entry.getKey() + ": " + connection.getHeaderField(entry.getKey()) );
	    }
	    System.out.println("------------------------------\n");
	   


	    //Get Response  
	    InputStream is = connection.getInputStream();
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	    StringBuilder strBld = new StringBuilder(); // or StringBuffer if not Java 5+ 

	    String line;
	    int numLine=0;
	    while((line = rd.readLine()) != null) {

		strBld.append(line);
		strBld.append('\n');
		numLine++;
	    }
	    rd.close();
	    is.close();


	    response= strBld.toString();

	}
	catch (SocketTimeoutException toe)
	{
	    toe.printStackTrace();
	    System.err.println("Timeout expired before the connection can be established!\nHave been transferred "+toe.bytesTransferred+" bytes.");
	    toe.getMessage();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
	finally {
	    if(connection != null) {
		connection.disconnect(); 
	    }
	}
	return response;
    }

    //Like get but does not print information on stdout
    private String gets(String targetURL)
    {
	String response=null;

	HttpsURLConnection connection = null;  
	try {
	    //Create connection
	    URL url = new URL(targetURL);
	    connection = (HttpsURLConnection)url.openConnection();
	    connection.setRequestMethod("GET");
	    connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
	    connection.setRequestProperty("User-Agent", "Firefox");
	    connection.setUseCaches(false);

	    connection.connect();

	    //Get Response  
	    InputStream is = connection.getInputStream();
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	    StringBuilder strBld = new StringBuilder(); // or StringBuffer if not Java 5+ 

	    String line;
	    int numLine=0;
	    while((line = rd.readLine()) != null) {

		strBld.append(line);
		strBld.append('\n');
		numLine++;
	    }
	    rd.close();
	    is.close();


	    response= strBld.toString();

	}
	catch (SocketTimeoutException toe)
	{
	    toe.printStackTrace();
	    System.err.println("Timeout expired before the connection can be established!\nHave been transferred "+toe.bytesTransferred+" bytes.");
	    toe.getMessage();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
	finally {
	    if(connection != null) {
		connection.disconnect(); 
	    }
	}
	return response;
    }


    //fromListToArray parses a csv list of elements of a HTTPS
    //response and assigns them to a String[]
    //First it counts response elements number to know the String[] length
    //Second it assigns those elements to String[]
    private String[] fromListToArray(String response)
    {	
	String[] repos;	
	int i=0;
	int level=0;
	boolean begin,end;
	int numEl=0;
	final String message="\"message\":";

	//Parsing response to count its elements
	for(i=0;i<response.length();i++)
	{
	    //Skip "body" content
	    if( (response.length()-i) > message.length() )
		if( response.substring(i,i+message.length()).equals(message) )
		{
		    for(i=i+message.length(); response.charAt(i)!='"' ; i++);
		    i++;
		    for( ; response.charAt(i)!='"' || response.charAt(i-1)=='\\' ; i++);
		    i++;
		}


	    if(level==0)
		begin=true;
	    else
		begin=false;

	    if(response.charAt(i)=='{')
	    {
		level++;
	    }
	    else if(response.charAt(i)=='}')
	    {
		level--;
	    }

	    if(begin && level==1)
		numEl++;
	}


	repos=new String[numEl];

	numEl=0;
	level=0;
	int beginIndex=0,endIndex=0;

	//Parsing response to assign its elements to repos[] array
	for(i=0;i<response.length();i++)
	{
	    //Skip "body" content
	    if( (response.length()-i) > message.length() )
		if( response.substring(i,i+message.length()).equals(message) )
		{
		    for(i=i+message.length(); response.charAt(i)!='"' ; i++);
		    i++;
		    for( ; response.charAt(i)!='"' || response.charAt(i-1)=='\\' ; i++);
		    i++;
		}

	    if(level==0)
	    {
		begin=true;
	    }
	    else
		begin=false;

	    if(level==1)
		end=true;
	    else
		end=false;


	    if(response.charAt(i)=='{')
	    {
		level++;
	    }
	    else if(response.charAt(i)=='}')
	    {
		level--;
	    }

	    if(begin && level==1)
	    {
		beginIndex=i+1;
		numEl++;
	    }

	    if(end && level==0)
	    {
		endIndex=i;
		repos[numEl-1]=response.substring(beginIndex,endIndex);
	    }

	}

	return repos;
    }

    //commits follows "commit_url" value for every repository passed as argument,
    //and for each commit found if its date comes after input date then it calls printCommit function
    //on new commit "url" with full informations, such as committed files,
    //and passing "full_name" value and "date" value.
    private void commits(String[] repos)
    {
	int i,j,k;
	final String commits_url="\"commits_url\":";
	final String full_name="\"full_name\":";
	final String commit="\"commit\":";
	final String message="\"message\":";
	final String url="\"url\":";
	final String date="\"date\":";
	String repoName;
	String[] commits;
	String url_value,date_value;
	int beginIndex,endIndex,level;
	SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat sdf2 =new SimpleDateFormat("dd-MM-yyyy");
	GregorianCalendar date2=new GregorianCalendar();

	for(i=0;i<repos.length;i++)
	{
	    //Get "commits_url" value of repository[i]
	    beginIndex=repos[i].indexOf(commits_url)+commits_url.length();
	    for( ; repos[i].charAt(beginIndex)!='"' ; beginIndex++);
	    beginIndex++;
	    for(endIndex=beginIndex;repos[i].charAt(endIndex)!='"' && repos[i].charAt(endIndex)!='{';endIndex++);
	    url_value=repos[i].substring(beginIndex,endIndex);

	    //Get "full_name" value of repository[i]
	    beginIndex=repos[i].indexOf(full_name)+full_name.length();
	    for( ; repos[i].charAt(beginIndex)!='"';beginIndex++);
	    beginIndex++;
	    for( endIndex=beginIndex; repos[i].charAt(endIndex)!='"';endIndex++);
	    repoName=repos[i].substring( beginIndex, endIndex );


	    commits=fromListToArray(gets(url_value));

	    if(commits.length>0)
		System.out.println(" Repository \""+repoName+"\" has commit(s)\n");
	    else
		System.out.println(" Repository \""+repoName+"\" has no commit\n");

	    for(j=0;j<commits.length;j++)
	    {
		//Skip \"commit\" content
		k=commits[j].indexOf(commit)+commit.length();
		for( ; commits[j].charAt(k)!='{' ; k++);
		k++;
		for(level=0;level>=0;k++)
		{
		    //Search and skip \"message\" subfield content
		    if( (commits[j].length()-k) > message.length() )
			if( commits[j].substring(k,k+message.length()).equals(message) )
			{
			    for(k+=message.length(); commits[j].charAt(k)!='"' ; k++);
			    k++;
			    for( ; commits[j].charAt(k)!='"' || commits[j].charAt(k-1)=='\\' ; k++);
			    k++;
			}

		    if(commits[j].charAt(k)=='{')
			level++;
		    else
			if(commits[j].charAt(k)=='}')
			    level--;
		}

	    	//Get "date" value
		beginIndex=commits[j].indexOf(date)+date.length();
		for( ; commits[j].charAt(beginIndex)!='"' ; beginIndex++);
		beginIndex++;
		for(endIndex=beginIndex;commits[j].charAt(endIndex)!='T';endIndex++);
		date_value=commits[j].substring( beginIndex, endIndex );
		
		try{
		    date2.setTime(sdf.parse(date_value));
		}
		catch(ParseException e){
		    e.printStackTrace();
		}

		if(this.date.compareTo(date2)<=0)
		{
		    //override url_value with new url of commit[j] of repos[i]
		    //which has full information about the commit
		    //including its files list
		    beginIndex=commits[j].indexOf(url,k)+url.length();
		    for( ; commits[j].charAt(beginIndex)!='"' ; beginIndex++);
		    beginIndex++;
		    for(endIndex=beginIndex;commits[j].charAt(endIndex)!='"' && commits[j].charAt(endIndex)!='{';endIndex++);
		    url_value=commits[j].substring(beginIndex,endIndex);

		    printCommit(repoName,gets(url_value),date2);
		}

		if(this.date.compareTo(date2)<=0)
		    System.out.println("\t commit["+(j+1)+"]\t"+sdf2.format(date2.getTime())+" | "+sdf2.format(this.date.getTime())+"\tV");
		else
		    System.out.println("\t commit["+(j+1)+"]\t"+sdf2.format(date2.getTime())+" | "+sdf2.format(this.date.getTime()));
	    }

	    System.out.println("-----------------------------------------------\n");
	}
    }

    //Get all needed informations about commit and use them to update "Commits" table
    private void printCommit(String repoName, String commit,GregorianCalendar date2)
    {
	final String sha="\"sha\":";
	final String login="\"login\":";
	final String filename="\"filename\":";
	int beginIndex,endIndex,k;
	String sha_value,login_value;
	StringBuilder strbld=new StringBuilder();
	
	
	//Get "sha": value
	beginIndex=commit.indexOf(sha)+sha.length();
	for( ; commit.charAt(beginIndex)!='"' ; beginIndex++);
	beginIndex++;
	for(endIndex=beginIndex;commit.charAt(endIndex)!='"' && commit.charAt(endIndex)!='{';endIndex++);
	sha_value=commit.substring(beginIndex,endIndex);
	
	//Get "login": value
	beginIndex=commit.indexOf(login)+login.length();
	for( ; commit.charAt(beginIndex)!='"' ; beginIndex++);
	beginIndex++;
	for(endIndex=beginIndex;commit.charAt(endIndex)!='"' && commit.charAt(endIndex)!='{';endIndex++);
	login_value=commit.substring(beginIndex,endIndex);
	
	//Get "filename": fields values
	for(k=0 ; ( k=commit.indexOf(filename,k) )!=-1 ; k=endIndex)
	{
	    beginIndex=k+filename.length();
	    for( ; commit.charAt(beginIndex)!='"' ; beginIndex++);
	    beginIndex++;
	    for(endIndex=beginIndex;commit.charAt(endIndex)!='"' && commit.charAt(endIndex)!='{';endIndex++);
	    strbld.append( commit.substring(beginIndex,endIndex) + "\n");
	}
	
	try{
	    pstmt.setString(1,repoName);
	    pstmt.setString(2,sha_value);
	    pstmt.setDate(3,new java.sql.Date( date2.getTime().getTime() ));
	    pstmt.setString(4,login_value);
	    pstmt.setString(5,strbld.toString());
	    pstmt.setString(6,commit);
	    pstmt.executeUpdate();
	}
	catch(SQLException ex){
	    System.err.println("An error occurred while updating the table\nSQLException: "+ex.getMessage());
	}
    }

    private void openDB()
    {
	String username;
	String password;
	String dbUrl;
	Statement stmt=null;

	username="postgres";
	password="postgres";
	dbUrl="jdbc:postgresql://localhost:5432/GetIssues";
	
	try{
	    //Required to manually load any drivers prior to JDBC 4.0
	    //Class.forName("org.postgresql.Driver");
	    dbCon=DriverManager.getConnection(dbUrl,username,password);
	    stmt = dbCon.createStatement();
	    stmt.executeUpdate("DROP TABLE IF EXISTS Commits");
	    stmt.executeUpdate("CREATE TABLE Commits" +
		    "(Full_Name varchar(50) NOT NULL, " +
		    "Sha varchar(50) NOT NULL, " +
		    "Date date NOT NULL, " +
		    "Committed_by varchar(50) NOT NULL, " +
		    "Files text NOT NULL, " +
		    "Commit text NOT NULL, " +
		    "PRIMARY KEY (Full_Name,Sha))");
	}
	/*
	catch(ClassNotFoundException ex){
	    System.err.println("ClassNotFoundException: "+ex.getMessage());
	    System.err.println("Driver jdbc non trovato");
	    System.exit(1);
	}
	*/
	catch(SQLException ex){
	    System.err.println("An error occurred accessing database");
	    System.err.println("SQLException: "+ex.getMessage());
	    System.exit(1);
	}
	finally{
	    if(stmt!=null)
		try{stmt.close();}
	    	catch(SQLException ex)
	    	{
		    System.err.println("SQLException: "+ex.getMessage());
		}
	}

	System.err.println("Database accessed with no errors, Issues table created");
    }
}//End of Class GitHub
