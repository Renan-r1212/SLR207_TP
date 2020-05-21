package slave;
/* 1. Criar um processo para criar a pasta maps usar && echo
 * 2. excutar a criação dos UMx.txt
 * 3. criar maps em outras maquinas e enviar UMx.txt(iterar sobre Sx para saber x correto) 
 */
import handoopMapReduce.FileWordCount;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Slave {
	private FileWordCount splitLineAcq;
	private ArrayList<String> machinesList;
	private List<String> wordsList;
	private ListIterator<String> wordIt;
	private ProcessBuilder pb;
	private Process process;
	private Integer splitNum;
	private String remoteMachine;
	private InetAddress ip;
	private String hostname;
	 
	public Slave() {
		splitLineAcq = new FileWordCount();
		wordsList	 = new ArrayList<String>();
		machinesList = new ArrayList<String>();
		process = null;
		splitNum = Integer.valueOf(0);
		
		try {
			ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
		} catch (UnknownHostException e){
			e.printStackTrace();
		}
	}
	
	
	public void deploySplits() {
		try {
			int splitFileNum = 0;
			BufferedReader read;
			read = new BufferedReader(new FileReader("machines.txt"));
			while((remoteMachine = read.readLine()) != null) {	
				machinesList.add(remoteMachine);
				
				pb = new ProcessBuilder("ssh", "rrodrigues@" + remoteMachine, "mkdir -p /tmp/rrodrigues/maps",
										"&& scp " + hostname +  ":/tmp/rrodrigues/splits/S"+ splitFileNum + ".txt", remoteMachine + ":/tmp/rrodrigues/splits/",
										"&& hostname");	
				process = pb.start();
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
	
	
	
	public List<String> wordsExtract(String file) {
		String splitLine;
		String[] words; 
		
		splitNum = Integer.parseInt(file.charAt(18) + "");
		
		splitLine = splitLineAcq.fileToString(file);
		words = splitLine.split(" ");
		
		return wordsList = Arrays.asList(words);
	}
	
	public List<String> mapList(List<String> splits) {
		int i = 0;
		
		wordIt = splits.listIterator();
		while(wordIt.hasNext()){
			splits.set(i, wordIt.next() + " 1");
			i++;
		}
		return splits;
	}
	
	public void saveMap(List<String> splits) {
		File file = new File("maps/UM" + splitNum + ".txt");
		BufferedWriter bf = null;

        try{
        	bf = new BufferedWriter( new FileWriter(file) );
        	
    		wordIt = splits.listIterator();
    		while(wordIt.hasNext()){
                bf.write(wordIt.next());
                bf.newLine();
    		}
            bf.flush();
            
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            
            try{
                //always close the writer
                bf.close();
            }catch(Exception e){}
        }
	}
	
	public static void main(String[] args) {
		
		Slave slv = new Slave();
		List<String> output = new ArrayList<String>();
		
		output = slv.mapList(slv.wordsExtract("/tmp/rrodrigues/splits/S0.txt"));
		System.out.print(output);
		
		slv.saveMap(output);		
	}
}
