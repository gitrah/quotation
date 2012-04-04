package com.hartenbower;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobbler extends Thread {
	
	StringBuffer output;
	/** The is. */
	InputStream is;

	/** The type. */
	String type;
	
	boolean debug = false;

	/**
	 * Instantiates a new stream gobbler.
	 * 
	 * @param is
	 *            the is
	 * @param type
	 *            the type
	 */
	StreamGobbler(InputStream is, String type) {
		this.is = is;
		this.type = type;
		output = new StringBuffer();
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if(debug) {
					System.out.println(type + ">" + line);
				}
				output.append(type + ">" + line);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public String output() {
		return output.toString();
	}
}
