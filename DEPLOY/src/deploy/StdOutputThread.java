package deploy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.LinkedBlockingQueue;

public class StdOutputThread extends Thread {
	private Process process;
	private BufferedReader reader;
	private boolean running;
	private StringBuilder strBuilder;
	private LinkedBlockingQueue<String> timeOutList;
	private String line;
	private String stdOutput;
	
	public StdOutputThread(LinkedBlockingQueue<String> _timeOutList, Process _process) {
		process = _process;
		timeOutList = _timeOutList;
		
		reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		line = null;
		strBuilder = new StringBuilder();
		stdOutput = null;
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
		    	 stdOutput = strBuilder.toString();
			     if(stdOutput != null && stdOutput != " ") {
			    	 System.out.println(stdOutput);
				     timeOutList.put(stdOutput);
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