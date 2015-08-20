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

public class GetCommit
{
    private String url;
    private GregorianCalendar date;
    private PreparedStatement pstmt;
    private Connection dbCon;
    
    GetCommit(String url,GregorianCalendar date){
	this.url=url;
	this.date=date;
	this.dbCon=null;
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
	

    String gets(String targetURL)
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
	    System.out.println("Response Code : " + connection.getResponseCode());
 
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
	    System.out.println("Timeout expired before the connection can be established!\nHave been transferred "+toe.bytesTransferred+" bytes.");
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

    String get(String targetURL)
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
	    System.out.println("Timeout expired before the connection can be established!\nHave been transferred "+toe.bytesTransferred+" bytes.");
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

    String[] fromListToArray(String response)
    {	
	String[] repos;	
	int i=0;
	int level=0;
	boolean begin,end;
	int numEl=0;
	final String message="\"message\":";

	//Scorro l'intera risposta per contare quanti elementi
	//appartegono al vettore
	for(i=0;i<response.length();i++)
	{
	    //Ignoro il contenuto del campo "message":
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

	//Scorro l'intera risposta per prelevare gli elementi del
	//vettore da assegnare a repos[]
	for(i=0;i<response.length();i++)
	{
	    //Ignoro il contenuto del campo "message":
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

    void getCommits(String[] repos)
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
	GregorianCalendar date2=new GregorianCalendar();

	for(i=0;i<repos.length;i++)
	{
	    //leggo il campo "commit_url":
	    beginIndex=repos[i].indexOf(commits_url)+commits_url.length();
	    for( ; repos[i].charAt(beginIndex)!='"' ; beginIndex++);
	    beginIndex++;
	    for(endIndex=beginIndex;repos[i].charAt(endIndex)!='"' && repos[i].charAt(endIndex)!='{';endIndex++);
	    url_value=repos[i].substring(beginIndex,endIndex);

	    //Leggo il campo "full_name": del repository[i]
	    beginIndex=repos[i].indexOf(full_name)+full_name.length();
	    for( ; repos[i].charAt(beginIndex)!='"';beginIndex++);
	    beginIndex++;
	    for( endIndex=beginIndex; repos[i].charAt(endIndex)!='"';endIndex++);
	    repoName=repos[i].substring( beginIndex, endIndex );


	    commits=fromListToArray(get(url_value));

	    for(j=0;j<commits.length;j++)
	    {
		//Salto il contenuto dell'elemento \"commit\"
		k=commits[j].indexOf(commit)+commit.length();
		for( ; commits[j].charAt(k)!='{' ; k++);
		k++;
		for(level=0;level>=0;k++)
		{
		    //Ignoro il contenuto del campo "message":
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

		//sovrascrivo url_value con il nuovo url del
		//singolo commit[j] di repos[i] contenente
		//informazioni dettagliate, inclusa la lista di files
		beginIndex=commits[j].indexOf(url,k)+url.length();
		for( ; commits[j].charAt(beginIndex)!='"' ; beginIndex++);
		beginIndex++;
		for(endIndex=beginIndex;commits[j].charAt(endIndex)!='"' && commits[j].charAt(endIndex)!='{';endIndex++);
		url_value=commits[j].substring(beginIndex,endIndex);

		//Leggo il campo "date":
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
		    printCommit(repoName,gets(url_value),date2);
	    }
	}
	
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

    void printCommit(String repoName, String commit,GregorianCalendar date2)
    {
	final String sha="\"sha\":";
	final String login="\"login\":";
	final String filename="\"filename\":";
	int beginIndex,endIndex,k;
	String sha_value,login_value;
	StringBuilder strbld=new StringBuilder();
	
	
	//leggo il campo "sha":
	beginIndex=commit.indexOf(sha)+sha.length();
	for( ; commit.charAt(beginIndex)!='"' ; beginIndex++);
	beginIndex++;
	for(endIndex=beginIndex;commit.charAt(endIndex)!='"' && commit.charAt(endIndex)!='{';endIndex++);
	sha_value=commit.substring(beginIndex,endIndex);
	
	//leggo il campo "login":
	beginIndex=commit.indexOf(login)+login.length();
	for( ; commit.charAt(beginIndex)!='"' ; beginIndex++);
	beginIndex++;
	for(endIndex=beginIndex;commit.charAt(endIndex)!='"' && commit.charAt(endIndex)!='{';endIndex++);
	login_value=commit.substring(beginIndex,endIndex);
	
	//leggo i campi "filename":
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
	    System.err.println("SQLException: "+ex.getMessage()+"\nErrore durante l'inserimento dei dati");
	}
    }


    void openDB()
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
	    System.err.println("SQLException: "+ex.getMessage());
	    System.err.println("Accesso al database non riuscito");
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

	System.err.println("Accesso al DB effettuato correttamente e tabella Commits creata");
    }
}//End of Class GetCommit
