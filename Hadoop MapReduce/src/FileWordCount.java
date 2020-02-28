import java.io.*;
import java.util.*;

public class FileWordCount {
	// fileToString Variables
	private String line,everything;
	private BufferedReader br;
	private StringBuilder sb;
	
	// countAndDisplay Variables
	private HashMap<String, Integer> wordOcc;
	private ListIterator<String> wordIt;
	private List<String> wordsList;
	private String fileStr,itStr;
	private String[] words;
	private int prevValue;
	private Map<String, Integer> sortedWords;
	

	public FileWordCount() {
		wordsList = new ArrayList<String>();
		wordOcc = new HashMap<String, Integer>();
	}
	
	public String fileToString(String file) {
		try{
			br = new BufferedReader(new FileReader(file));
		    sb = new StringBuilder();
		    line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        line = br.readLine();
		        if(line != null)
		        	sb.append(' ');
		    }
		    everything = sb.toString();
		    
		}catch(Exception e){}
		return everything;
	}
	
	public void countAndDisplay(String file) {		
		fileStr = fileToString(file);			
		words = fileStr.split(" ");
		wordsList = Arrays.asList(words);
		
		wordIt = wordsList.listIterator();
		while(wordIt.hasNext()){
			itStr = wordIt.next();
			if(wordOcc.containsKey(itStr)) {
				prevValue = wordOcc.get(itStr);
				wordOcc.replace(itStr, (prevValue + 1));				
			}else {
				wordOcc.put(itStr, 1);		
			}
		}
		sortedWords = new TreeMap<String, Integer>(wordOcc);
		for(String key : sortedWords.keySet()) {
			System.out.println(key + " " + sortedWords.get(key));
		}
	}
}
