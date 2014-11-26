package com.hartenbower;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.junit.Test;

import scala.Tuple2;

public class CountTest {
	public static List<Quotation> qods = Qutil.qods;
	public static TreeSet<String> keys = null;
	public static TreeSet<Integer> counts = null;
	public static TreeSet<Integer> lengths = null;
	public static Map<String, Integer> wordCountMap = new HashMap<String,Integer>();
	public static Map<Integer, List<String>> countWordsMap = new HashMap<Integer, List<String>>();
	public static Map<Integer, List<String>> lengthWordsMap = new HashMap<Integer, List<String>>();
	public static Map<Integer, char[]> lengthAcharMap = new HashMap<Integer, char[]>();
	public static Map<Integer, Integer> wordLengthWordCountMap = new HashMap<Integer,Integer>();

	// for each word, holds a list of tuples; first is index of quotation in qods, second is index into body of quote
	// the quote must be 'reconstructed' after the search to determine if phrases match (which would correspond to words 
	// in the same quote with successive indices)
	public static Map<String, List<Tuple2<Integer,Integer>>> wordQuoteIndices = new HashMap<String, List<Tuple2<Integer,Integer>>>();
	
	@Test
	public void testCount() {
		long ltime = System.currentTimeMillis();
		
		//String delim = "[ \\.,;:\"'-?!\n\t\\$0123456789#\\(\\)/%&\\*\\[\\]]|[^\\w,\\w]++";
		String delim = "[ \\.\\,;:\"'-?!\n\t\\$0123456789#\\(\\)/%&\\*\\[\\]<=–…>\\+]";
		String word ;
		List<Tuple2<Integer,Integer>> quindices;
		int qindex = 0;
		for (Quotation q : Qutil.qods) {
			 StringTokenizer tk = new StringTokenizer( q.getBody(), delim);
			 while(tk.hasMoreTokens()) {
				 word = tk.nextToken().trim();
				 Integer cnt = wordCountMap.get(word);
				 cnt = (cnt == null) ? 1 : cnt+1;
				 wordCountMap.put(word, cnt);
				 quindices = wordQuoteIndices.get(word);
				 if(quindices == null) {
					 quindices = new ArrayList<Tuple2<Integer,Integer>>();
					 wordQuoteIndices.put(word,quindices);
				 }
				 quindices.add(new Tuple2<Integer,Integer>(qindex, q.getBody().indexOf(word)));
			 }
			 qindex++;
		}
		long countsTime = System.currentTimeMillis() - ltime;
		System.out.println("\n\n\ncounts took " +( Qutil.fromMillis(countsTime)) + "\n\n\n");
		keys = new TreeSet<String>(wordCountMap.keySet());
		Iterator<String> ik = keys.descendingIterator();
		while(ik.hasNext()) {
			word = ik.next();
			System.out.println(word + " " + wordCountMap.get(word));
		}
		System.out.println();
		System.out.println("count-Words (list of words with same count) map");
		System.out.println();
		sortByCount();
		System.out.println();
		System.out.println("length-count (length of word - count of words of that length) map");
		System.out.println();
		countsByLength();
	}

	public static void sortByCount() {
		Iterator<String> ikeys = keys.iterator();
		String word;
		Integer count;
		List<String> words;
		while(ikeys.hasNext()) {
			word = ikeys.next();
			count = wordCountMap.get(word);
			words = countWordsMap.get(count);
			if(words == null) {
				words = new ArrayList<String>();
				words.add(word);
				countWordsMap.put(count,words);
			} else words.add(word);
		}
		counts = new TreeSet<Integer>(countWordsMap.keySet());
		Iterator<Integer> icounts = counts.descendingIterator();
		int wsize;
		while(icounts.hasNext()) {
			count = icounts.next();
			words = countWordsMap.get(count);
			wsize =words.size();
			System.out.println(count + " " + words.size() + " " +
					(wsize < 12 ? 
					words :
					(words.subList(0, 9) +  "...(+ " + (wsize - 10) + " others)" ) ));
		}
	}
	
	public static void countsByLength() {
		Iterator<String> ikeys = keys.iterator();
		Integer length;
		Integer count;
		String word;
		List<String> words;
		while(ikeys.hasNext()) {
			word = ikeys.next();
			length = word.length();
			words = lengthWordsMap.get(length);
			if(words == null) {
				words = new ArrayList<String>();
				lengthWordsMap.put(length, words);
			}
			words.add(word);
			count = wordLengthWordCountMap.get(length);
			if(count == null) {
				count = 0;
			}
			count += wordCountMap.get(word); // add counts of this word to slot for its word length
			wordLengthWordCountMap.put(length, count);
		}
		lengths = new TreeSet<Integer>(wordLengthWordCountMap.keySet());
		Iterator<Integer> ilengths = lengths.iterator();
		int wsize;
		while(ilengths.hasNext()) {
			length = ilengths.next();
			count = wordLengthWordCountMap.get(length);
			words = lengthWordsMap.get(length);
			wsize =words.size();
			System.out.println(length + " " + count + " " +
					(wsize < 12 ? 
					words :
					(words.subList(0, 9) +  "...(+ " + (wsize - 10) + " others)" ) ));
		}
	}
	
	public static void arraysByLength() {
		Iterator<Integer> ilengths = lengths.iterator();
		int length,count;
		List<String> words;
		String word;
		while(ilengths.hasNext()) {
			length = ilengths.next();
			words = lengthWordsMap.get(length);
			count = words.size();
			System.out.println("have " + count + " different words of length " +length);
			char[] buff = new char[length * count];
			int rowOffset, colOffset;
			for(int i=0; i < count; i++) {
				word = words.get(i);
				for(int j = 0; j < length; j++) {
					rowOffset = j * count; 
					buff[rowOffset + i] = word.charAt(j);
				}
			}
			lengthAcharMap.put(length, buff);
			File f = new File("len"+length+"array.ser");
			ObjectOutputStream oos  = null;
			try {
				oos= new ObjectOutputStream(new FileOutputStream(f));
				oos.write(length);
				oos.write(count);
				oos.writeObject(buff);
				oos.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
	
