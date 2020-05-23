package master.slave;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class master {
	private ArrayList<Thread> slaveSTDsList;
	private ArrayList<Thread> errorThreadsList;
	private ArrayList<String> machinesList;
	private ArrayList<Process> processesList;
	private ArrayList<LinkedBlockingQueue<String>> stdOutputQueueList;
	private ArrayList<LinkedBlockingQueue<String>> errorQueueList;
	private LinkedBlockingQueue<String> errorTimeOutQueue;
	private LinkedBlockingQueue<String> stdOutputTimeOutQueue;
	private slaveSTD stdThread;
	private slaveError errThread;
	private Thread[] mapThreadArray;
	
	Map<String, String> machineUMxMap;
	Map<String, ArrayList<String>> wordsUMxMap;
	
	private ProcessBuilder pb;
	private Process process;
	private String output;
	private BufferedReader read;
	private String remoteMachine;
	private InetAddress ip;
	private String hostname;
	private int splitFileNum;
	
	public master(){
		machinesList = new ArrayList<String>();
		machineUMxMap = new HashMap<String, String>();
		wordsUMxMap = new HashMap<String, ArrayList<String>>();
		output = null;
		splitFileNum = 0;
		
		try {
			ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
		} catch (UnknownHostException e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//% java -jar slave.jar 
		
		master m = new master();
		m.deploySplits();
	}
	
	public void deploySplits() {
		try {
			System.out.println("Deploying splits...");
			read = new BufferedReader(new FileReader("../machines.txt"));
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				read.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	void generateMaps() {
		System.out.println("Generating maps...");
		
		try {	
	
			for(int i = 0; i < machinesList.size(); i++) {	
				
				// Cria o processos para tenatar realizar a conexção remota a
				// maquina especifica e o armazena em um Arraylist de processos
				pb = new ProcessBuilder("ssh", "rrodrigues@" + machinesList.get(i),
										"java", "-jar",	"/tmp/rrodrigues/Slave.jar map S" + i + ".txt");	
				process = pb.start();
				processesList.add(process);
				
				stdOutputTimeOutQueue	= new LinkedBlockingQueue<String>();
				stdOutputQueueList.add(stdOutputTimeOutQueue);
				errorTimeOutQueue		= new LinkedBlockingQueue<String>();
				errorQueueList.add(stdOutputTimeOutQueue);
				
				stdThread = new slaveSTD(stdOutputTimeOutQueue, process);
				slaveSTDsList.add(stdThread);
				errThread = new slaveError(errorTimeOutQueue, process);
				errorThreadsList.add(errThread);
				
				stdThread.start();
				errThread.start();			
			}
			
			mapThreadArray = new Thread[slaveSTDsList.size()];
			
			for(int i = 0; i < mapThreadArray.length; i++) {
				mapThreadArray[i] = new MapReducedSupport(i);
				mapThreadArray[i].start();
			}
			
			for(int i = 0; i < mapThreadArray.length; i++ ) {
				mapThreadArray[i].join();
			}
			
			System.out.println("\tMapping Results\nUMx -- Machine");
			for (String key : machineUMxMap.keySet()) {
				System.out.println(key + " - " + machineUMxMap.get(key));
			}

			System.out.println("\nKey -- Um List");
			for (String key : wordsUMxMap.keySet()) {
				System.out.println(key + " - " + wordsUMxMap.get(key));
			}
			System.out.println("\n");
			
		}  catch (InterruptedException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				read.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	class MapReducedSupport extends Thread {
		private int num; 
		
		public MapReducedSupport(int _i) {
			num = _i;
		}
		
		@Override
		public void run() {
			
			String stdOutput;
			String errorOutput;
			
			try {
				stdOutput = stdOutputQueueList.get(num).poll(5, TimeUnit.SECONDS);
				errorOutput = errorQueueList.get(num).poll(5, TimeUnit.SECONDS);
				
				if(stdOutput == null && errorOutput == null) {
					System.out.print("Machine - " + machinesList.get(num) + ": TIME OUT");
					
					((slaveSTD) slaveSTDsList.get(num)).terminate();
					((slaveError) errorThreadsList.get(num)).terminate();
					
					slaveSTDsList.get(num).join();
					errorThreadsList.get(num).join();
					
					process.destroy();
				} else if(stdOutput != null && stdOutput != "") {
					System.out.println("Machine - " + machinesList.get(num) + ": Output\n\n" + stdOutput + "\n");
					machineUMxMap.put(machinesList.get(num), "UM" + num);
					
					String[] outputKeys = output.split("[\\r\\n]+");
					
					for(int i = 0; i < outputKeys.length; i++) {
						
						if (wordsUMxMap.containsKey(outputKeys[i])) {
							ArrayList<String> l = wordsUMxMap.get(outputKeys[i]);
							l.add("UM" + num);
							wordsUMxMap.put(outputKeys[i], l);
						} else {
							ArrayList<String> l = new ArrayList<String>();
							l.add("UM" + num);
							wordsUMxMap.put(outputKeys[i], l);
						}
					}
				} else {
					System.out.println("Machine - " + machinesList.get(num) + ": Error\n\n" + stdOutput + "\n");
				} 	
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}

}
