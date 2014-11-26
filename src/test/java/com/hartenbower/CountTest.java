package com.hartenbower;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
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
	public static int countOfQods = qods.size();
	public static int wordCount = 0;
	public static boolean printCounts = false;
	public static TreeSet<String> words = null;
	public static TreeSet<Integer> wordCounts = null;
	public static TreeSet<Integer> wordLengths = null;
	public static TreeSet<Short> distinctQuoteLengths  = null;

	public static List<Short> quoteLengths = new ArrayList<Short>();
	public static Map<Short, Short> quoteLengthQuoteCountMap = new HashMap<Short,Short>();
	public static Map<Short, Integer> binnedQuoteLengthQuoteCountMap = new HashMap<Short,Integer>();
	public static short maxBodyLength = 0;
	public static short numberOfBins = 0;
	public static short binWidth = 256;
	
	public static Map<String, Integer> wordCountMap = new HashMap<String,Integer>();
	public static Map<Integer, List<String>> countWordsMap = new HashMap<Integer, List<String>>();
	public static Map<Integer, List<String>> lengthWordsMap = new HashMap<Integer, List<String>>();
	public static Map<Integer, char[]> lengthAcharMap = new HashMap<Integer, char[]>();
	public static Map<Integer, Integer> wordLengthWordCountMap = new HashMap<Integer,Integer>();

	// for each word, holds a list of tuples; first is index of quotation in qods, second is index into body of quote
	// the quote must be 'reconstructed' after the search to determine if phrases match (which would correspond to words 
	// in the same quote with successive indices)
	public static Map<String, List<Tuple2<Integer,Short>>> wordQuoteIndices = new HashMap<String, List<Tuple2<Integer,Short>>>();
	
	
	public static int sum(Collection<Short> col) {
		int s = 0;
		Iterator<Short> i = col.iterator();
		while(i.hasNext()) {
			s += i.next();
		}
		return s;
	}
	public static int sumi(Collection<Integer> col) {
		int s = 0;
		Iterator<Integer> i = col.iterator();
		while(i.hasNext()) {
			s += i.next();
		}
		return s;
	}
	
	public static void main(String... args) {
		new CountTest().testCount();
	}
	
	@Test
	public void testCount() {
		long ltime = System.currentTimeMillis();
		
		//String delim = "[ \\.,;:\"'-?!\n\t\\$0123456789#\\(\\)/%&\\*\\[\\]]|[^\\w,\\w]++";
		String delim = "[ \\.\\,;:\"'-?!\n\t\\$0123456789#\\(\\)/%&\\*\\[\\]<=–…>\\+]";
		String word ;
		List<Tuple2<Integer,Short>> quindices;
		int qindex = 0;
		short bodyLength;
		
		System.out.println("Loading " + countOfQods + " quotations");
		for (Quotation q : Qutil.qods) {
			 bodyLength = (short)q.getBody().length();
			 quoteLengths.add(bodyLength);
	      	 maxBodyLength = (short)Math.max(maxBodyLength, bodyLength);
			 StringTokenizer tk = new StringTokenizer( q.getBody(), delim);
			 while(tk.hasMoreTokens()) {
				 word = tk.nextToken().trim();
				 Integer cnt = wordCountMap.get(word);
				 cnt = (cnt == null) ? 1 : cnt+1;
				 wordCountMap.put(word, cnt);
				 quindices = wordQuoteIndices.get(word);
				 if(quindices == null) {
					 quindices = new ArrayList<Tuple2<Integer,Short>>();
					 wordQuoteIndices.put(word,quindices);
				 }
				 quindices.add(new Tuple2<Integer,Short>(qindex, (short)q.getBody().indexOf(word)));
			 }
			 qindex++;
		}
		long countsTime = System.currentTimeMillis() - ltime;
		System.out.println("\n\n\ncounts took " +( Qutil.fromMillis(countsTime)) + "\n\n\n");
		wordCount = sumi(wordCountMap.values());
		System.out.println("sum of word counts "  + wordCount);
		
		words = new TreeSet<String>(wordCountMap.keySet());
		if(printCounts) {
			Iterator<String> ik = words.descendingIterator();
			while(ik.hasNext()) {
				word = ik.next();
				System.out.println(word + " " + wordCountMap.get(word));
			}
		}
		System.out.println();
		System.out.println("count-Words (list of words with same count) map");
		System.out.println();
		sortByCount();
		System.out.println();
		System.out.println("length-count (length of word - count of words of that length) map");
		System.out.println();
		countsByLength();
		System.out.println();
		System.out.println("length-count (length of quote - count of quotes of that length) map");
		System.out.println();
		countsByBodyLength();
		System.out.println();
		System.out.println("binned length-count (bin (range of length of quote) - count of quotes of that length bin) map");
		System.out.println();
		binnedCountsByBodyLength();
	}

	public static void sortByCount() {
		Iterator<String> ikeys = words.iterator();
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
		wordCounts = new TreeSet<Integer>(countWordsMap.keySet());
		Iterator<Integer> icounts = wordCounts.descendingIterator();
		int wsize;
		while(icounts.hasNext()) {
			count = icounts.next();
			words = countWordsMap.get(count);
			wsize =words.size();
			System.out.println(count + " " + words.size() + " " +
					(wsize < 11 ? 
					words :
					(words.subList(0, 11)+ (wsize > 10 ? ("...(+ " + (wsize - 10) + " others)" ) : "" ) )));
		}
	}
	
	public static void countsByLength() {
		Iterator<String> ikeys = words.iterator();
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
		
		System.out.println("sum of word counts 'sumi(wordCountMap.values())' "  + sumi(wordCountMap.values()));
		assert(sumi(wordLengthWordCountMap.values()) == wordCount);

		wordLengths = new TreeSet<Integer>(wordLengthWordCountMap.keySet());
		Iterator<Integer> ilengths = wordLengths.iterator();
		int wsize;
		while(ilengths.hasNext()) {
			length = ilengths.next();
			count = wordLengthWordCountMap.get(length);
			words = lengthWordsMap.get(length);
			wsize =words.size();
			System.out.println(length + " " + count + " " +
					(wsize < 11 ? 
					words :
					(words.subList(0, 11) + (wsize > 10 ? ("...(+ " + (wsize - 10) + " others)" ) : "" ) )));
		}
	}
	
	public static void arraysByLength() {
		Iterator<Integer> ilengths = wordLengths.iterator();
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
	
	public static void countsByBodyLength() {
		Short count;
		quoteLengthQuoteCountMap.clear();
		for(Short length : quoteLengths) {
			count = quoteLengthQuoteCountMap.get(length);
			count = count == null ? (short) 1 :(short)(count+1);
			quoteLengthQuoteCountMap.put(length, count);
		}
		
		System.out.println("found " + quoteLengthQuoteCountMap.size() + "  distinct quote lengths");
		assert(sum(quoteLengthQuoteCountMap.values()) == countOfQods);
		distinctQuoteLengths = new TreeSet<Short>(quoteLengthQuoteCountMap.keySet());
		Iterator<Short> ilengths = distinctQuoteLengths.iterator();
		Short length;
		while(ilengths.hasNext()) {
			length = ilengths.next();
			count = quoteLengthQuoteCountMap.get(length);
			System.out.println(length + " " + count);
		}
	}
	
	public static void binnedCountsByBodyLength() {
		Iterator<Short> ilengths = distinctQuoteLengths.iterator();
		Short length;
		Short quoteCount;
		Integer quoteCountInBin;
		if(numberOfBins == 0) {
			numberOfBins = (short) ( maxBodyLength/binWidth);
		} else if(binWidth == 0) {
			binWidth = (short) (maxBodyLength / numberOfBins);
		}
		int quoteTotal = 0;
		binnedQuoteLengthQuoteCountMap.clear();
		while(ilengths.hasNext()) {
			length = ilengths.next();
			quoteCount = quoteLengthQuoteCountMap.get(length);
			quoteTotal += quoteCount;
			boolean foundBin = false;
			for(short i = 0; i < numberOfBins && !foundBin; i++) {
				if(length > i * binWidth && ( length <= (i+1) * binWidth || i == numberOfBins-1)) {
					quoteCountInBin = binnedQuoteLengthQuoteCountMap.get(i);
					quoteCountInBin = (quoteCountInBin == null) ? quoteCount : quoteCountInBin + quoteCount;
					binnedQuoteLengthQuoteCountMap.put(i, quoteCountInBin);
					foundBin = true;
				}
			}
			assert(foundBin);
		}
		System.out.println("quoteTotal " + quoteTotal);
		int quoteCountAllBins = sumi(binnedQuoteLengthQuoteCountMap.values());
		System.out.println("quoteCountAllBins " + quoteCountAllBins);
	
		System.out.println("using " + numberOfBins + " bins, found count of quote length range");
		TreeSet<Short> bins = new TreeSet<Short>(binnedQuoteLengthQuoteCountMap.keySet());
		Iterator<Short> ibins= bins.iterator();
		Short bin;
		int binMin;
		while(ibins.hasNext()) {
			bin = ibins.next();
			binMin =bin* binWidth;
			quoteCountInBin = binnedQuoteLengthQuoteCountMap.get(bin);
			System.out.println("bin " + bin  + " (" + binMin + " - " + (binMin + binWidth) + "] "+ quoteCountInBin);
		}
		assert(quoteCountAllBins == countOfQods);
	}
	
}
	
