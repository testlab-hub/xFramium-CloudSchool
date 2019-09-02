package org.xframium.console.http.handler.spi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.xframium.console.http.handler.ECHandler;
import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
public class OpenFile extends ECHandler
{

	@Override
	protected byte[] _handle(HttpExchange httpExchange) 
	{	
	    try
	    {
    		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    		
    		System.out.println( "Getting [" + "org/xframium/console/http/html/" + httpExchange.getRequestURI().toString() + "]" );
    		
    		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream( "org/xframium/console/http/html" + httpExchange.getRequestURI().toString() );
    		
    		byte[] buffer = new byte[512];
    		int bytesRead = 0;
    		
    		while ( (bytesRead = inputStream.read( buffer ) ) > 0 )
    		    outputStream.write( buffer, 0, bytesRead );
    		
    		inputStream.close();
    		
    		return outputStream.toByteArray();
    		
	    }
	    catch( Exception e )
	    {
	        return e.getMessage().getBytes();
	    }
	}
}
