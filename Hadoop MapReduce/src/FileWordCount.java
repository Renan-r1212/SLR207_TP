import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

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
	private Map<String, Integer> sortedByCount;
	private String outputFilePath;
	private long startTime;
	private long endTime;
	private long totalTimeAlpha;
	private long totalTimeOcc;
	

	public FileWordCount() {
		wordsList = new ArrayList<String>();
		wordOcc = new HashMap<String, Integer>();
		outputFilePath = "/cal/homes/rrodrigues/Telecom_Paris/SLR/SLR207/SLR207_TP/Hadoop MapReduce/output.txt";
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
	
	
	public Map<String, Integer> sortByValue(final Map<String, Integer> wordCounts) {
		
        return wordCounts.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
	
	
	public void hashMapToTxt(final Map<String, Integer> sortedHashMap, long _totalTimeOcc, long _totalTimeAlpha) {
        File file = new File(outputFilePath);
        
        BufferedWriter bf = null;;
        
        try{
            
            //create new BufferedWriter for the output file
            bf = new BufferedWriter( new FileWriter(file) );
 
            //iterate map entries
            bf.write("Frequency count time: " + _totalTimeOcc);
            bf.newLine();
            bf.write("Alphabetical sort time: " + _totalTimeAlpha);
            bf.newLine();
            
            for(String key : sortedHashMap.keySet()){
                
                //put key and value separated by a colon
                bf.write( key + " " + sortedHashMap.get(key));
                
                //new line
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
	
	public void countAndDisplay(String file) {	
		startTime = System.currentTimeMillis();
		
		fileStr = fileToString(file);
		fileStr = fileStr.replaceAll("\\p{P}","");
		words = fileStr.split(" ");
		wordsList = Arrays.asList(words);
		
		wordIt = wordsList.listIterator();
		while(wordIt.hasNext()){
			itStr = wordIt.next();
			if(!itStr.isBlank()) {
				itStr = itStr.toLowerCase();
				if(wordOcc.containsKey(itStr)) {
					prevValue = wordOcc.get(itStr);
					wordOcc.replace(itStr, (prevValue + 1));				
				}else {
					wordOcc.put(itStr, 1);		
				}
			}
		}
        endTime = System.currentTimeMillis();
        totalTimeOcc = endTime - startTime;
		
		sortedWords = new TreeMap<String, Integer>(wordOcc);
		
        endTime = System.currentTimeMillis();
        totalTimeAlpha = endTime - startTime;
        
        sortedByCount = sortByValue(sortedWords);		

        hashMapToTxt(sortedByCount, totalTimeOcc, totalTimeAlpha);
        System.out.print("Output generated");
	}
}
