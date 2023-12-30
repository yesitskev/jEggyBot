// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								PLUGINLOADER
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot;

import java.io.*;

import java.net.URL;
import java.net.URLDecoder;

public class PluginLoader extends ClassLoader
{
    // ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************

	public PluginLoader()
	{

	}
	
    // ********************************************************************************
    //          PRIVATE METHODS
    // ********************************************************************************
	
	private byte[] loadClassData(String filePath) throws IOException
	{
		// load file and get file size
		File file = new File(filePath);
		int size = (int) file.length();
		// allocate buffer space
		byte buffer[] = new byte[size];
		// load file data
		FileInputStream fis = new FileInputStream(filePath);
		fis.read(buffer);
		fis.close();
		return buffer;
	}
	
    // ********************************************************************************
    //          PUBLIC METHODS
    // ********************************************************************************

	public Class loadClass(String className) throws ClassNotFoundException
	{
    	return findClass(className);
	}
	
	public Class findClass(String className)
	{
		// attempt to load class from primordial class loader
		// if this class doesn't belong to the plugins package
		if (!className.startsWith("plugins.")) {
			try {
				return findSystemClass(className);
			}
			catch (ClassNotFoundException e) {
			}
		}
		// file was not found in system path, so try to locate file and load
		// the actual class data
		try {
			String filename = className.replace('.', File.separatorChar) + ".class";
			// attempt to locate file path from system class path
			URL url = ClassLoader.getSystemResource(filename);
			String filePath = URLDecoder.decode(url.getPath(), "UTF-8");
			// load class data from file
			byte[] classByte = loadClassData(filePath);
			// convert string of bytes into java class object and return result
			Class result = defineClass(className, classByte, 0, classByte.length, null);
			return result;
		}
		catch (IOException e) {
			return null;
		}
	}
}