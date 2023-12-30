// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								ARCHIVERESOURCES
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot.util;

import java.io.DataInputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

public class ArchiveResources
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private HashMap<String, byte[]> files;		// list of files in archive
	
    // ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************

	public ArchiveResources(String filename)
	{
		// initialisation
		files = new HashMap<String, byte[]>();
		try {
			// open zip file for reading file entries and sizes
			ZipFile zip = new ZipFile(filename);
			// read through contents of file
			Iterator it = (files.values()).iterator();
			while (it.hasNext()) {
				ZipEntry entry = (ZipEntry) it.next();
				if (!entry.isDirectory()) {
					// open data input stream to file within compressed archive
					DataInputStream dis = new DataInputStream(zip.getInputStream(entry));
					// allocate buffer space to resource
					byte[] buffer = new byte[dis.available()];
					// read entire stream into buffer
					dis.readFully(buffer);
					// close stream
					dis.close();
					// add file entry to files list
					files.put(entry.getName(), buffer);
				}	
			}
		}
		catch (IOException e) {
		}
	}
	
    // ********************************************************************************
    //          PUBLIC METHODS
    // ********************************************************************************

	public byte[] extract(String filename)
	{
		return (byte[]) files.get(filename);
	}

	public HashMap getFileList()
	{
		return files;
	}

	public boolean containsFile(String filename)
	{
		return files.containsKey(filename);
	}
}