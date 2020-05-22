package slave;
/* 1. Criar um processo para criar a pasta maps usar && echo
 * 2. excutar a criação dos UMx.txt
 * 3. criar maps em outras maquinas e enviar UMx.txt(iterar sobre Sx para saber x correto) 
 */

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
	private ArrayList<String> machinesList;
	private List<String> wordsList;
	private ListIterator<String> wordIt;
	private ProcessBuilder pb;
	private Process process;
	private Integer splitNum;
	private String remoteMachine;

	 
	public Slave() {
		wordsList	 = new ArrayList<String>();
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
		System.out.println("Deploying maps directorys");
		try {
			BufferedReader read;
			read = new BufferedReader(new FileReader("machines.txt"));
			while((remoteMachine = read.readLine()) != null) {	
				machinesList.add(remoteMachine);
				
				pb = new ProcessBuilder("ssh", "rrodrigues@" + remoteMachine, "mkdir -p /tmp/rrodrigues/maps",
										"&& hostname");	
				pb.inheritIO(); 
				process = pb.start();
				process.waitFor(); 
			}
			System.out.println("Map Directorys deployed");
			read.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Maps directorys deployed");
		}
	}
	
	public List<String> wordsExtract(String file) {
		String splitLine;
		String[] words; 
		
		splitNum = Integer.parseInt(file.charAt(1) + "");
		
		splitLine = fileToString("splits/" + file);
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
                bf.close();
            }catch(Exception e){}
        }
	}
	
	public void map(String fileName) {
		System.out.print("Mapping...");
		deployMapDir();
		saveMap(mapList(wordsExtract(fileName)));
		System.out.print("Map completed");
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
