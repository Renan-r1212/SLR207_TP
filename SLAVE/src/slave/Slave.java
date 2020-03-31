package slave;

public class Slave extends Thread{

	public static void main(String[] args) {
		
		try {
			Thread.sleep(10000);
		}catch(Exception e) {}
		
		System.err.print(3+5);

	}

}
