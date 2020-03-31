package deploy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.LinkedBlockingQueue;

public class ErrorThread extends Thread {
	private Process process;
	private boolean running;
	private BufferedReader reader;
	private StringBuilder strBuilder;
	private LinkedBlockingQueue<String> timeOutList;
	private String line;
	private String errorOutput;
	
	public ErrorThread(LinkedBlockingQueue<String> _timeOutList, Process _process) {
		process = _process;
		timeOutList = _timeOutList;
		
		reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		line = null;
		strBuilder = new StringBuilder();
		errorOutput = null;
		running = true;
	}

	void terminate() {
		 running = false;
	}
	
	@Override
	public void run(){
		try {       
		     while ((line = reader.readLine()) != null && running) {
		    	 strBuilder.append(line);
		    	 strBuilder.append(System.getProperty("line.separator"));
		     }
		    
		     if(running) {
			     errorOutput = strBuilder.toString();
			     if(errorOutput != null && errorOutput != " ") {
			    	 System.err.println(errorOutput);
			    	 timeOutList.put(errorOutput);
			     }	     
		     }
		    	 
		     reader.close();
		     
		} catch (IOException e) {
		    e.printStackTrace();
		} catch(InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
}
