package master.slave;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class master {
	private LinkedBlockingQueue<String> timeOutList;
	private ProcessBuilder pb;
	private Process process;
	private String output;
	
	public master(){
		timeOutList = new LinkedBlockingQueue<String>();
		pb = new ProcessBuilder("java", "-jar", "/home/renan/Documents/telecom_paris/SLR/SLR207/SLR207_TP/SLAVE/slave.jar");
		process = null;
		output = null;
	}
	
	public void outputReader() {
		try {
			process = pb.start();
			
			slaveSTD threadSTD = new slaveSTD(timeOutList, process);
			slaveError threadError = new slaveError(timeOutList, process);
			
			threadSTD.start();	
			threadError.start();
			
			output = timeOutList.poll(12, TimeUnit.SECONDS);
			if(output == null) {
				System.out.print("TIME OUT");
				
				threadSTD.terminate();
				threadError.terminate();
				
				threadSTD.join();
				threadError.join();
				
				process.destroy();
			}
			
		} catch(InterruptedException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//% java -jar slave.jar 
		
		master m = new master();
		m.outputReader();
	}
}
