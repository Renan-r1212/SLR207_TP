package slave;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Slave {
	private ArrayList<String> machinesList;
	private List<String> wordsList;
	private List<String> mapSplits;
	private ListIterator<String> wordIt;
	private ProcessBuilder pb;
	private Process process;
	private Integer splitNum;
	private String remoteMachine;

	 
	public Slave() {
		wordsList	 = new ArrayList<String>();
		mapSplits	 = new ArrayList<String>();
		machinesList = new ArrayList<String>();
		process = null;
		splitNum = Integer.valueOf(0);
		
	}
	
	/* FileToString takes as argument a string with the path name
	 * of a file and return it's lines with a space between each
	 * line.
	 * @param file: String
	 * 
	 * */
	
	public String fileToString(String file) {
		String everything = null;
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        line = br.readLine();
		        if(line != null)
		        	sb.append(' ');
		    }
		    
		    return everything = sb.toString();
		}catch(Exception e){}
		return everything = null;
	}
	
	public void deployMapDir() {
		try {
			BufferedReader read;
			read = new BufferedReader(new FileReader("../machines.txt"));
			while((remoteMachine = read.readLine()) != null) {	
				machinesList.add(remoteMachine);
				
				pb = new ProcessBuilder("ssh", "rrodrigues@" + remoteMachine, "mkdir -p /tmp/rrodrigues/maps",
										"&& hostname");	
				pb.inheritIO(); 
				process = pb.start();
				process.waitFor(); 
			}
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
		
		splitNum = Integer.parseInt(file.charAt(1) + "");
		
		splitLine = fileToString("/tmp/rrodrigues/splits/" + file);
		words = splitLine.split(" ");
		
		return wordsList = Arrays.asList(words);
	}
	
	public List<String> mapList(List<String> splits) {
		wordIt = splits.listIterator();
		while(wordIt.hasNext()){
			mapSplits.add(wordIt.next() + " 1");
		}
		return mapSplits;
	}
	
	//"/tmp/rrodrigues/maps/UM" + splitNum + ".txt"
	public void saveMap(List<String> splits) {
		PrintWriter fileOutput = null;
		Set<String> keys = new HashSet<String>();
		try {
			fileOutput = new PrintWriter((new FileWriter("/tmp/rrodrigues/maps/UM" + splitNum + ".txt")));
			for(int i = 0; i < splits.size(); i++) {
				fileOutput.println(splits.get(i));
				System.out.println(this.wordsList.get(i));
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			fileOutput.close();
		}
	}
	
	public void map(String fileName) {
		// passar p/ master System.out.print("Mapping...");
		// idem      System.out.print("Map completed");
		deployMapDir();
		
		System.out.print(mapList(wordsExtract(fileName)));
		
	}

	
	public static void main(String[] args) {	
		
		int N = args.length;
		if (N < 2) {
			System.err.println("No arguments");
		} else if (args[0] == "map") {
				Slave s = new Slave();
				s.map(args[1]);
			}
		
	}
}
