/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection.
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring
* the fact that the entirety of the webserver execution might be handling
* other clients, too.
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format).
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;

public class WebWorker implements Runnable
{

String fileName;
private Socket socket;


/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
    socket = s;
}//end WebWorker

/**
* Worker thread starting point. Each worker handles just one HTTP
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
	String urlAdd;
	String contentType;

    System.err.println("Handling connection...");
    try
    {
        InputStream  is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        urlAdd = readHTTPRequest( is );
        System.out.println( urlAddress );
        
		if(urlAdd.contains(".gif"))
			contentType = "image/gif";
		else if(urlAdd.contains(".jpg"))
			contentType = "image/jpg";
		else if(urlAdd.contains(".png"))
			contentType = "image/png";
		else if(urlAdd.contains(".ico"))
			contentType = "image/x-icon";
		else
			contentType = "text/html";
        
		writeHTTPHeader(os,contentType,urlAdd);
		writeContent(os,contentType,urlAdd);
        os.flush();
        socket.close();
    } catch ( Exception e )
    {
        System.err.println( "Output error: " + e );
    }//end try/catch
    System.err.println( "Done handling connection." );
    return;
}//end run

/**
* Read the HTTP request header.
**/
private String readHTTPRequest( InputStream is )
{
    String line;
    BufferedReader read = new BufferedReader( new InputStreamReader( is ) );
    String urlAdd = "";

    while ( true )
    {
        try
        {
            while ( !read.ready() ) Thread.sleep( 1 );
            line = read.readLine();
            if ( line.contains( "GET " ) ) 
            {
                urlAdd = line.substring( 4 );
                for( int i = 0 ; i < urlAdd.length() ; i++ )
                {
                    if ( urlAdd.charAt( i ) == ' ' )
                        urlAdd = urlAdd.substring(0 , i );
                }//end for
            }//end if
            System.err.println( "Request line: (" + line + ")" );
            if ( line.length() == 0 )
                break;
        } catch ( Exception e )
        {
            System.err.println( "Request error: " + e );
            break;
        }//end try/catch
    }//end while
    return urlAdd;

}//end readHTTPRequest

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader( OutputStream os , String contentType , String urlAddress ) throws Exception
{
    Date dt = new Date();
    DateFormat dtForm = DateFormat.getDateTimeInstance();
    df.setTimeZone( TimeZone.getTimeZone("GMT") );
    os.write( "HTTP/1.1 200 OK\n".getBytes() );
    os.write( "Date: ".getBytes());
    os.write( ( dtForm.format( dt ) ).getBytes() );
    os.write( "\n".getBytes() );
    os.write( "Server: Guillermo's very own server\n".getBytes() );
    os.write( "Connection: close\n".getBytes() );
    os.write( "Content-Type: ".getBytes() );
    os.write( contentType.getBytes() );
    os.write( "\n\n".getBytes() ); // HTTP header ends with 2 newlines
    return;
}//end writeHTTPHeader



private void writeHTTP404Error(OutputStream os, String contentType, String urlAddress) throws Exception
{
    Date dt = new Date();
    DateFormat dtForm = DateFormat.getDateTimeInstance();
    df.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
    os.write( "HTTP/1.1 404 ERROR\n".getBytes() );
    os.write( "Date: ".getBytes() );
    os.write( ( dtForm.format(dt)).getBytes() );
    os.write( "\n".getBytes() );
    os.write( "Server: Guillermo's very own server\n".getBytes() );
    //os.write( "Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes() );
    //os.write( "Content-Length: 438\n".getBytes() );
    os.write( "Connection: close\n".getBytes() );
    os.write( "Content-Type: ".getBytes() );
    os.write( contentType.getBytes() );
    os.write( "\n\n".getBytes() ); // HTTP header ends with 2 newlines
    os.write("<html><head></head><body>\n".getBytes());
    os.write("<h2>ERROR: 404 Not Found</h2>\n".getBytes());
    os.write("</body></html>\n".getBytes());
   return;
}//end writeHTTP404Error

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os, String contentType, String urlAdd) throws Exception
{
	Date dt = new Date();
	DateFormat dtForm = DateFormat.getDateTimeInstance();
	dtForm.setTimeZone(TimeZone.getTimeZone( "GMT" ));
	String dataContent = "";
	String urlAdrsCopy = "." + urlAdd.substring( 0 , urlAdd.length( ) );
	String date = dtForm.format( dt );
	
	if(contentType.equals("test/html"))
	{
		try
		{
			File url = new File( urlAdrsCopy );
			FileReader urlRead = new FileReader( url );
			BufferedReader urlBuffer = new BufferedReader( urlRead );
			while( ( dataContent = urlBuffer.readLine() ) != null )
			{
				os.write( dataContent.getBytes() );
				os.write( "\n".getBytes());
				if ( dataContent.contains( "<cs371date>" ) )
				{
					os.write( date.getBytes() );
					os.write( "\n".getBytes() );
				}//end if
				if ( dataContent.contains( "<cs371server>" ) )
					os.write( "Guillermo's Server ID string\n".getBytes() );
			}//end while
			
		} catch(FileNotFoundException e)
		{
			writeHTTP404Error( os , "text/html" , urlAdd );
		}//end try/catch
	}//end if
	else if(contentType.contains("image"))
	{
		FileInputStream imageReader = new FileInputStream(in);
		byte imgArray[] = new byte[(int)in.length()];
		imageReader.read(imgArray);
		DataOutputStream imgOut = new DataOutputStream(os);
	I	imageOut.write(imgArray);
	}//end else if
}//end writeContent

} // end class
