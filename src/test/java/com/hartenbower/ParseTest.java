package com.hartenbower;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

import com.hartenbower.Quotation;
import com.hartenbower.FileUtil;
import com.hartenbower.QParser;

public class ParseTest {

	@Test
	public void testParse() {
		QParser.parseRecursive("<<path to downloaded copy of site to be slurped>>");
		
		if(!QParser.quotes.isEmpty()) {
			List<Quotation> qs = QParser.quotes;
			qs.addAll(QParser.oldQuotes);
			System.out.println("tot " + qs.size());
			FileUtil.toFile(qs, "newQods.ser");
			System.out.println("dups " + QParser.dupCount);
			
			List<Quotation> nq = (List) FileUtil.fromFile("newQods.ser", Quotation.class);
			System.out.println("loaded " + nq.size());
		}
		
		//assertTrue(...)
		
	}
}
