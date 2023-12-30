// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								CURRENCYFETCHER
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins.CurrencyConverter;

import java.io.*;

import java.net.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CurrencyFetcher
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private String currency1;
	private String currency2;
	private boolean valid = false;
	private double rate;
	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
	
	public CurrencyFetcher(String currency1, String currency2) throws IOException
	{
		// store from currency and to currency
		this.currency1 = currency1.toUpperCase();
		this.currency2 = currency2.toUpperCase();
		// build cgi request
		String request = "Amount=1" + "&From=" + currency1 + "&To=" + currency2;
		// resolve address to ip
		InetAddress ip = InetAddress.getByName("xe.com");
		// connect to server
		SocketAddress sockaddr = new InetSocketAddress(ip, 80);
		Socket socket = new Socket();
		socket.setSoTimeout(4000);
		socket.connect(sockaddr, 3000);
		// send http header and request
		BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		bfw.write("POST /ucc/convert.cgi HTTP/1.0\r\n");
		bfw.write("Content-Length: " + request.length() + "\r\n");
		bfw.write("Content-Type: application/x-www-form-urlencoded\r\n");
		bfw.write("User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; YPC 3.0.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)\r\n");
		bfw.write("\r\n");
		bfw.write(request);
		bfw.flush();
		// retrieve response
		BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		Pattern pattern = Pattern.compile("1 " + currency1 + " = (.+) " + currency2);
		Matcher matcher;
		String data;
		while ((data = bfr.readLine()) != null) {
			// remove trailing spaces
			data = data.trim();
			if (data.equalsIgnoreCase("<title>xe.com - universal currency converter results</title>")) {
				// we know that we have a proper result
				valid = true;
			}
			// parse through to line which states conversion rate
			matcher = pattern.matcher(data);
			if (matcher.matches()) {
				rate = Double.parseDouble(matcher.group(1));
			}
		}
		// close socket
		socket.close();
		// close streams
		bfr.close();
		bfw.close();
	}
	
	// ********************************************************************************
    //          ACCESSOR + MUTATOR METHODS
    // ********************************************************************************
	
	public String getCurrency1()
	{
		return currency1;
	}
	
	public String getCurrency2()
	{
		return currency2;
	}
	
	public boolean isValid()
	{
		return valid;
	}
	
	public double getRate()
	{
		return rate;
	}
}