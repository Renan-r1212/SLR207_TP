package handoopMapReduce;

public class WordOccurrence {

	public static void main(String[] args) {
		FileWordCount x = new FileWordCount();
		
		x.countAndDisplay("/tmp/CC-MAIN-20170322212949-00140-ip-10-233-31-227.ec2.internal.warc.wet");
	}
}