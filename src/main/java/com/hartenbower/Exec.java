package com.hartenbower;


import java.io.File;
import java.util.Map;

/**
 * The Class Exec.
 */
public class Exec {
	
	boolean debug = false;
	Process process;
	StreamGobbler errorGobbler;

	// any output?
	StreamGobbler outputGobbler;
	/**
	 * Instantiates a new exec.
	 */
	public Exec() {
	}


	public StreamGobbler getErrorGobbler() {
		return errorGobbler;
	}


	public void setErrorGobbler(StreamGobbler errorGobbler) {
		this.errorGobbler = errorGobbler;
	}


	public StreamGobbler getOutputGobbler() {
		return outputGobbler;
	}


	public void setOutputGobbler(StreamGobbler outputGobbler) {
		this.outputGobbler = outputGobbler;
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
		if(errorGobbler != null) {
			errorGobbler.setDebug(debug);
		}
		if(outputGobbler != null) {
			outputGobbler.setDebug(debug);
		}
	}


	/**
	 * Command.
	 * 
	 * @param cmd
	 *            the cmd
	 * 
	 * @return the int
	 */
	public int command(String... cmd) {
		int exitVal = -1;

		try {
			
			ProcessBuilder pb = new ProcessBuilder(cmd);
			Map<String, String> env = pb.environment();
			process = pb.start();
		    
		    
		    
			// any error message?
			errorGobbler = new StreamGobbler(
			        process.getErrorStream(), "ERROR");
			errorGobbler.setDebug(debug);
			// any output?
			outputGobbler = new StreamGobbler(
			        process.getInputStream(), "OUTPUT");
			outputGobbler.setDebug(debug);

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			// any error???
			exitVal = process.waitFor();
			System.out.println("ExitValue: " + exitVal);
		} catch (Throwable t) {
			t.printStackTrace();
			exitVal = 1;
			
		}

		return exitVal;
	}
	
	public File getBash(){
		//try linux/mac
		File bash = new File("/bin/sh");
		if (!bash.exists()){
			//try cygwin 
			bash = new File("/cygwin/bin/sh");
		}
		return bash;
	}


    public Process getProcess() {
        return process;
    }


    public void setProcess(Process process) {
        this.process = process;
    }

}
