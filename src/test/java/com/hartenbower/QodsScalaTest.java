package com.hartenbower;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.junit.Test;

import com.hartenbower.Quotation;
import com.hartenbower.Qutil;

public class QodsScalaTest {
	static List<Quotation> qods = Qutil.qods;
	static long actors, single;
	@Test
	public void testScalaQods() {
		long ltime = System.currentTimeMillis();
		System.out.println("\n\nsearching with all cores\n\n");
		QuoteWorker.setQuotations(qods);
		System.out.println("\nsearching 'four scare' with ld = 3\n");
		QuoteWorker.search("four scare", 3);
		System.out.println("\nsearching 'wigte night' with ld = 3\n");
		QuoteWorker.search("wigte night", 3);
		System.out.println("\nsearching 'uthinasia' with ld = 3\n");
		QuoteWorker.search("uthinasia", 3);
		System.out.println("\nsearching 'brewtopia' with ld = 4\n");
		QuoteWorker.search("brewtopia", 4);
		actors = System.currentTimeMillis() - ltime;
		System.out.println("\n\n\nactors took " +(Qutil.fromMillis(actors)  ) + "\n\n\n");
	}

	@Test
	public void testQods() {
		long ltime = System.currentTimeMillis();
		System.out.println("\n\nsearching with one core (will take longer...)\n\n");
		System.out.println("\nsearching 'four scare' with ld = 3\n");
		List<Quotation> res = Qutil.search("four scare", 3);
		System.out.println("\nsearching 'wigte night' with ld = 3\n");
		res.addAll(Qutil.search("wigte night", 3));
		System.out.println("\nsearching 'uthinasia' with ld = 3\n");
		res.addAll(Qutil.search("uthinasia", 3));
		System.out.println("\nsearching 'brewtopia' with ld = 4\n");
		res.addAll(Qutil.search("brewtopia", 4));
		if(!res.isEmpty()) {
			System.out.println("found " + res.size());
			for(Quotation q : res) {
				System.out.println(q);
			}
		}
		single = System.currentTimeMillis() - ltime;
		System.out.println("\n\n\njava took " +( Qutil.fromMillis(single)) + "\n\n\n");
		System.out.println("ratio " + (single/(1. * actors)) );
		
	}
	
}
