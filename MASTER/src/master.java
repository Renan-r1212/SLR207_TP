import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class master {

	public static void main(String[] args) {
		//% java -jar slave.jar      
		 ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/rrodrigues/slave.jar");
		 
		 try {
			 Process process = pb.start();
			 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

	            String line;
	            while ((line = reader.readLine()) != null) {
	                System.out.println(line);
	            }
		 }catch (IOException e) {
			 System.out.println(e);
		 }
	}
}
