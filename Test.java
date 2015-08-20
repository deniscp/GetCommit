import java.io.*;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;


public class Test{
    public static void main(String args[])
    {
	String targetURL=null;
	String data=null;

	if(args.length != 2)
	{
	    System.out.println("Test https://url dd-MM-yyyy");
	    return;
	}

	targetURL=args[0];
	data=args[1];

	//targetURL="https://api.github.com/users/deniscp/repos";
	//targetURL="https://api.github.com/orgs/octokit/repos";
	//data="01-07-2015";
	
	/*
	targetURL="https://api.github.com/repos/octokit/go-octokit";
	targetURL="https://api.github.com/repos/octokit/octokit.rb";
	targetURL="https://api.github.com/repos/octokit/octokit.objc";
	targetURL="https://api.github.com/repos/octokit/octokit.net";
	targetURL="http://localhost/nuovo/issues3";
	targetURL="https://api.github.com/repos/octokit/octokit.net/issues?page=1";
	*/

	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	GregorianCalendar date=new GregorianCalendar();
	try{
	    date.setTime(sdf.parse(data));
	}
	catch (ParseException e){
	    e.printStackTrace();
	}

	GetCommit conn=new GetCommit(targetURL,date);

	String response=conn.get(targetURL);
	String[] repos=conn.fromListToArray(response);


	conn.getCommits(repos);	


    }//end of main
}//end of Test class


