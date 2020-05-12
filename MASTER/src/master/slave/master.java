package master.slave;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class master {
	private LinkedBlockingQueue<String> timeOutList;
	private ArrayList<String> machinesList;
	private ProcessBuilder pb;
	private Process process;
	private String output;
	private BufferedReader read;
	private String remoteMachine;
	private InetAddress ip;
	private String hostname;
	private int splitFileNum;
	
	public master(){
		timeOutList  = new LinkedBlockingQueue<String>();
		machinesList = new ArrayList<String>();
		pb = new ProcessBuilder("java", "-jar", "/home/renan/Documents/telecom_paris/SLR/SLR207/SLR207_TP/SLAVE/slave.jar");
		process = null;
		output = null;
		splitFileNum = 0;
		
		try {
			ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
		} catch (UnknownHostException e){
			e.printStackTrace();
		}
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
	
	public void deploySplits() {
		try {
			read = new BufferedReader(new FileReader("machines.txt"));
			while((remoteMachine = read.readLine()) != null) {	
				machinesList.add(remoteMachine);
				
				pb = new ProcessBuilder("ssh", "rrodrigues@" + remoteMachine, "mkdir -p /tmp/rrodrigues/splits",
										"&& echo splits directory " + splitFileNum + " deployed",
										"&& scp " + hostname +  ":/tmp/rrodrigues/splits/S"+ splitFileNum + ".txt", remoteMachine + ":/tmp/rrodrigues/splits/",
										"&& hostname");	
				pb.start();
				splitFileNum++;
			}
			System.out.println("Splits deployed");
			read.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//% java -jar slave.jar 
		
		master m = new master();
		m.deploySplits();
	}
}
