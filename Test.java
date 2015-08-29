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


	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	GregorianCalendar date=new GregorianCalendar();
	try{
	    date.setTime(sdf.parse(data));
	}
	catch (ParseException e){
	    e.printStackTrace();
	}

	GitHub conn=new GitHub(targetURL,date);

	conn.getCommit();
	conn.getLimits();

    }//end of main
}//end of Test class


