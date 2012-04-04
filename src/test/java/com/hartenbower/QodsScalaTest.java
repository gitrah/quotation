package com.hartenbower;

import java.util.List;

import org.junit.Test;

import com.hartenbower.Quotation;
import com.hartenbower.Qutil;

public class QodsScalaTest {
	static List<Quotation> qods = Qutil.qods;
	static long actors, single;
	@Test
	public void testScalaQods() {
		long ltime = System.currentTimeMillis();
		QuoteWorker.setQuotations(qods);
		QuoteWorker.search("four scare", 3);
		QuoteWorker.search("wigte night", 3);
		QuoteWorker.search("uthinasia", 3);
		QuoteWorker.search("brewtopia", 4);
		actors = System.currentTimeMillis() - ltime;
		System.out.println("\n\n\nactors took " +(Qutil.fromMillis(actors)  ) + "\n\n\n");
	}

	@Test
	public void testQods() {
		long ltime = System.currentTimeMillis();
		List<Quotation> res = Qutil.search("four scare", 3);
		res.addAll(Qutil.search("wigte night", 3));
		res.addAll(Qutil.search("uthinasia", 3));
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
