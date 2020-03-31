package deploy;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Deploy {
	private ArrayList<Thread> stdOutputThreadsList;
	private ArrayList<Thread> errorThreadsList;
	private ArrayList<String> machinesList;
	private ArrayList<Process> processesList;
	private ArrayList<LinkedBlockingQueue<String>> stdOutputQueueList;
	private ArrayList<LinkedBlockingQueue<String>> errorQueueList;
	private LinkedBlockingQueue<String> errorTimeOutQueue;
	private LinkedBlockingQueue<String> stdOutputTimeOutQueue;
	private BufferedReader read;
	private String remoteMachine;
	private ProcessBuilder pb;
	private Process process;
	private StdOutputThread stdThread;
	private ErrorThread errThread;
	private String stdOutput;
	private String errorOutput;
	
	
	public Deploy() {
		stdOutputThreadsList 	= new ArrayList<Thread>();
		errorThreadsList 		= new ArrayList<Thread>();
		machinesList		 	= new ArrayList<String>();
		processesList			= new ArrayList<Process>();
		stdOutputQueueList		= new ArrayList<LinkedBlockingQueue<String>>();
		errorQueueList			= new ArrayList<LinkedBlockingQueue<String>>();
		
		errorTimeOutQueue		= null;
		stdOutputTimeOutQueue	= null;
		remoteMachine			= null;
		pb						= null;
		process					= null;
		stdThread				= null;
		errThread				= null;
		stdOutput				= null;
		errorOutput				= null;	
	}
	
	/** 
	 * verifyConnection():
	 * 
	 * Cette méthode est utilisée pour vérifier que la connexion à distance
	 * aux machines énumérées sous "machines". Pour la vérification, un
	 * processus différent a été créé pour accéder à chaque machine, aux
	 * threads pour recevoir la sortie de commande ou tout message d'erreur.
	 * Une linklist liée a été utilisée pour gérer le TimeOut. Les processus,
	 * les threads et les linkedlists utilisés ont été stockés dans les
	 * Arraylists respectives pour être accessibles et pour pouvoir traiter la
	 * réponse de la commande en fonction de la machine en question.
	 * 
	 */
	
	void verifyConnection() {
		try {
			read = new BufferedReader(new FileReader("machines.txt"));
			
			while((remoteMachine = read.readLine()) != null) {	
				machinesList.add(remoteMachine);
				
				// Cria o processos para tenatar realizar a conexção remota a
				// maquina especifica e o armazena em um Arraylist de processos
				pb = new ProcessBuilder("ssh", "rrodrigues@" + remoteMachine);
				process = pb.start();
				processesList.add(process);
				
				stdOutputTimeOutQueue	= new LinkedBlockingQueue<String>();
				stdOutputQueueList.add(stdOutputTimeOutQueue);
				errorTimeOutQueue		= new LinkedBlockingQueue<String>();
				errorQueueList.add(stdOutputTimeOutQueue);
				
				stdThread = new StdOutputThread(stdOutputTimeOutQueue, process);
				stdOutputThreadsList.add(stdThread);
				errThread = new ErrorThread(errorTimeOutQueue, process);
				errorThreadsList.add(errThread);
				
				stdThread.start();
				errThread.start();			
			}
			
			read.close();
			
			for(int i = 0; i<errorThreadsList.size(); i++) {
				stdOutput = stdOutputQueueList.get(i).poll(5, TimeUnit.SECONDS);
				errorOutput = errorQueueList.get(i).poll(5, TimeUnit.SECONDS);
				
				if(stdOutput == null && errorOutput == null) {
					System.out.print("Process " + i + ": TIME OUT");
					
					((StdOutputThread) stdOutputThreadsList.get(i)).terminate();
					((StdOutputThread) errorThreadsList.get(i)).terminate();
					
					stdOutputThreadsList.get(i).join();
					errorThreadsList.get(i).join();
					
					process.destroy();
				} else if(stdOutput != null && errorOutput == null) {
					System.out.println("Process " + i + ":\n\n" + stdOutput + "\n");
				} else {
					System.err.println("Process " + i + ":\n\n" + errorOutput + "\n");
				}
			}
			
		}  catch (InterruptedException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Deploy deploy = new Deploy();
		deploy.verifyConnection();

	}

}
