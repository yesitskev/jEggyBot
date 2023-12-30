// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								FILEDOWNLOAD
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins.Wget;

import java.io.*;

import java.net.*;

public class FileDownload
{
	// ********************************************************************************
	//			DECLARATIONS
	// ********************************************************************************
	
	private String downloader;			// nickname of user who initiated download
	private String address;				// url of file to download
	private String filename;			// filename
	private String path;				// path to save file to
	private URLConnection connection;	// connection
	private InputStream in;				// connection input stream
	private FileOutputStream fos;		// file output stream
	private long totalReceived = 0;		// total bytes received
	private long startTime;				// time when file transfer started
	
	// ********************************************************************************
	//			CONSTRUCTOR
	// ********************************************************************************
	
	public FileDownload(String downloader, String address, String filename, String path)
	{
		// store nickname of downloader, address, local filename and path to save file to
		this.downloader = downloader;
		this.address = address;
		this.filename = filename;
		this.path = path;
	}
	
	// ********************************************************************************
    //          ACCESSOR +	MUTATOR	METHODS
    // ********************************************************************************
	
	public String getDownloader()
	{
		return downloader;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	public String getFilename()
	{
		return filename;
	}
	
	public long getBytesReceived()
	{
		return totalReceived;
	}
	
	// ********************************************************************************
	//			PUBLIC METHODS
	// ********************************************************************************
	
	public void download() throws MalformedURLException, IOException
	{
		// create download path if directory doesn't already exist
		File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		// create new url and open connection
		URL url = new URL(address);
		connection = url.openConnection();
		connection.setConnectTimeout(4000);
		connection.setReadTimeout(4000);
		// store current time so that we can calculate the transfer rate
		startTime = System.currentTimeMillis();
		// open input stream for file
		in = connection.getInputStream();
		// open file output stream to write data to
		fos = new FileOutputStream(path + "/" + filename);
		// allocate buffer space
		byte[] buffer = new byte[1024];
		// begin downloading of file
		int bytesRead;
		while ((bytesRead = in.read(buffer)) != -1) {
			fos.write(buffer, 0, bytesRead);
			totalReceived += bytesRead;
		}
		fos.flush();
		// close input and output streams
		in.close();
		fos.close();
	}
	
	public long getTransferRate()
	{
		// calculate time that has elapsed since transfer began
		long seconds = (System.currentTimeMillis() - startTime) / 1000;
		if (seconds > 0) {
			// divide the total number of bytes received so far by the number of seconds that have
			// elapsed
			long rate = totalReceived / seconds;
			return rate;
		}
		else {
			return 0;
		}
	}
	
	public int getPercentComplete()
	{
		return (int) (((double) totalReceived / connection.getContentLength()) * 100);
	}
	
	public void cancel()
	{
		// close input and output streams
		try {
			in.close();
			fos.close();
		}
		catch (IOException e) {
		}
		// delete incomplete file
		File file = new File(path + "/" + filename);
		file.delete();
		
	}
}