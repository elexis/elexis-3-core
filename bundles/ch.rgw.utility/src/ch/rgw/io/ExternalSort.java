package ch.rgw.io;

// filename: ExternalSort.java
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

/**
 * Goal: offer a generic external-memory sorting program in Java.
 * 
 * It must be : - hackable (easy to adapt) - scalable to large files - sensibly efficient.
 * 
 * This software is in the public domain.
 * 
 * By Daniel Lemire, April 2010 http://www.daniel-lemire.com/
 */
public class ExternalSort {
	
	/**
	 * This will simply load the file by blocks of x rows, then sort them in-memory, and write the
	 * result to a bunch of temporary files that have to be merged later.
	 * 
	 * @param file
	 *            some flat file
	 * @return a list of temporary flat files
	 */
	public static List<File> sortInBatch(File file, Comparator<String> cmp) throws IOException{
		List<File> files = new Vector<File>();
		BufferedReader fbr = new BufferedReader(new FileReader(file));
		long totalrowread = 0;
		try {
			List<String> tmplist = new Vector<String>();
			String line = "";
			try {
				while (line != null) {
					tmplist = new Vector<String>();
					while ((Runtime.getRuntime().freeMemory() > 2097152)
						&& ((line = fbr.readLine()) != null)) { // as long
						// as you
						// have 2MB
						tmplist.add(line);
					}
					files.add(sortAndSave(tmplist, cmp));
					tmplist.clear();
				}
			} catch (EOFException oef) {
				if (tmplist.size() > 0) {
					files.add(sortAndSave(tmplist, cmp));
					tmplist.clear();
				}
			}
		} finally {
			fbr.close();
		}
		return files;
	}
	
	public static File sortAndSave(List<String> tmplist, Comparator<String> cmp) throws IOException{
		Collections.sort(tmplist, cmp);
		File newtmpfile = File.createTempFile("sortInBatch", "flatfile");
		newtmpfile.deleteOnExit();
		BufferedWriter fbw = new BufferedWriter(new FileWriter(newtmpfile));
		try {
			for (String r : tmplist) {
				fbw.write(r);
				fbw.newLine();
			}
		} finally {
			fbw.close();
		}
		return newtmpfile;
	}
	
	/**
	 * This merges a bunch of temporary flat files
	 * 
	 * @param files
	 * @param output
	 *            file
	 */
	public static int mergeSortedFiles(List<File> files, File outputfile, Comparator<String> cmp)
		throws IOException{
		PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<BinaryFileBuffer>();
		for (File f : files) {
			BinaryFileBuffer bfb = new BinaryFileBuffer(f, cmp);
			pq.add(bfb);
		}
		BufferedWriter fbw = new BufferedWriter(new FileWriter(outputfile));
		int rowcounter = 0;
		try {
			while (pq.size() > 0) {
				BinaryFileBuffer bfb = pq.poll();
				String r = bfb.pop();
				fbw.write(r);
				fbw.newLine();
				++rowcounter;
				if (bfb.empty()) {
					bfb.fbr.close();
					bfb.originalfile.delete();// we don't need you anymore
				} else {
					pq.add(bfb); // add it back
				}
			}
		} finally {
			fbw.close();
		}
		return rowcounter;
	}
	
	public static void main(String[] args) throws IOException{
		if (args.length < 2) {
			System.out.println("please provide input and output file names");
			return;
		}
		String inputfile = args[0];
		String outputfile = args[1];
		Comparator<String> comparator = new Comparator<String>() {
			public int compare(String r1, String r2){
				return r1.compareTo(r2);
			}
		};
		List<File> l = sortInBatch(new File(inputfile), comparator);
		mergeSortedFiles(l, new File(outputfile), comparator);
	}
	
	public void sortFile(File in, File out, Comparator<String> cmp) throws IOException{
		List<File> l = sortInBatch(in, cmp);
		mergeSortedFiles(l, out, cmp);
	}
	
	static class BinaryFileBuffer implements Comparable<BinaryFileBuffer> {
		public static int BUFFERSIZE = 512;
		public BufferedReader fbr;
		private List<String> buf = new Vector<String>();
		int currentpointer = 0;
		Comparator<String> mCMP;
		public File originalfile;
		
		public BinaryFileBuffer(File f, Comparator<String> cmp) throws IOException{
			originalfile = f;
			mCMP = cmp;
			fbr = new BufferedReader(new FileReader(f));
			reload();
		}
		
		public boolean empty(){
			return buf.size() == 0;
		}
		
		private void reload() throws IOException{
			buf.clear();
			try {
				String line;
				while ((buf.size() < BUFFERSIZE) && ((line = fbr.readLine()) != null))
					buf.add(line);
			} catch (EOFException oef) {}
		}
		
		public String peek(){
			if (empty())
				return null;
			return buf.get(currentpointer);
		}
		
		public String pop() throws IOException{
			String answer = peek();
			++currentpointer;
			if (currentpointer == buf.size()) {
				reload();
				currentpointer = 0;
			}
			return answer;
		}
		
		public int compareTo(BinaryFileBuffer b){
			return mCMP.compare(peek(), b.peek());
		}
		
	}
}
