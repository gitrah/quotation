package com.hartenbower;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.htmlparser.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import com.hartenbower.Quotation;

/*
 * This was implemented for a particular site's DOM
 * For new site, reimplement parseNode and maybe parse and parseRecursive
 * or throw this mess out and do it with scala 
 */
public class QParser {
	static Logger log = Logger.getLogger(Parser.class);
	public static List<Quotation> quotes = new LinkedList<Quotation>();
	static Quotation lastQuote;
	static int found = 0, lastCount = 0;
	static List<Quotation> oldQuotes = Qutil.qods;
	public static int dupCount = 0;
	
	public static boolean dupQ(Quotation q) {
		for (Quotation t : oldQuotes) {
			if (q.getBody().equals(t.getBody())) {
				dupCount++;
				return true;
			}
		}
		return false;
	}

	public static void parseRecursive(String path) {
		System.out.println("now " + path);
		File f = new File(path);

		if (f.isDirectory()) {
			String[] paths = f.list();
			for (String p : paths) {
				p = path.endsWith(File.separator) ? path + p : path
						+ File.separator + p;
				File fp = new File(p);
				if (fp.isDirectory()) {
					parseRecursive(p);
				} else if (p.endsWith("html")) {
					parse(p);
				}
			}
		} else {
			parse(path);
		}
	}

	public static void parse(String filename) {
		System.out.println("parsing " + filename);
		int oldCount = quotes.size();
		if (new File(filename).exists()) {
			try {

				Tidy tidy = new Tidy(); // obtain a new Tidy instance
				tidy.setXHTML(false); // set desired config options using tidy
										// setters
				FileInputStream fis = new FileInputStream(filename);
				Document dom = tidy.parseDOM(fis, null);
				fis.close();
				NodeList nl = dom.getChildNodes();
				System.out.println("at root have " + nl.getLength());
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					log.info(parseNode(0, n));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("no file " + filename);
		}
		int addedCount = quotes.size() - oldCount;

		System.out.println(filename + " --->  found " + found + " but list is "
				+ addedCount);
		if (quotes.size() - lastCount > 10) {
			System.out.println("total " + quotes.size());
		}

	}

	static Node lastDt = null, lastDD = null;

	static boolean parentOrGrandParent(Node n1, Node n2) {
		Node parent = n2.getParentNode();
		if (parent != null) {
			if (n1.equals(parent)) {
				return true;
			}
			Node gp = parent.getParentNode();
			if (gp != null) {
				return n1.equals(gp);
			}
		}
		return false;
	}

	static private String parseNode(int i, Node n) {

		String spaces = "";
		while (i-- > 0) {
			spaces += " ";
		}
		StringBuffer sb = new StringBuffer(spaces);
		String name = n.getNodeName();
		NamedNodeMap map = n.getAttributes();
		if ("span".equals(name)) {
			Node nn = map.getNamedItem("class");
	        //log.info("nn.getNodeValue() " + nn.getNodeValue());
			if ("body".equals(nn.getNodeValue())) {
				NodeList kids = n.getChildNodes();
				if(kids.getLength() == 1 && "#text".equals(kids.item(0).getNodeName())){
					Node kid = kids.item(0);
					String body = kid.getNodeValue();
					log.info("kid " +kid.getNodeName() + " :: " +  kid.getNodeValue());
					
					found++;
					if (lastQuote != null) {
						if(!dupQ(lastQuote)) {
							quotes.add(lastQuote);
						}
						lastQuote = null;
					}
					lastQuote = new Quotation();
					lastQuote.setBody(kid.getNodeValue());
					lastDt = n;
				}
			} else if("bodybold".equals(nn.getNodeValue()) && lastQuote != null && lastQuote.getAuthor() ==  null) {
                NodeList kids = n.getChildNodes();
                for (int j = 0; j < kids.getLength(); j++) {
                    Node kid = kids.item(j);
                    NodeList gkids = kid.getChildNodes();
                    log.info("gkids " + gkids.getLength());
                    for (int k = 0; k < gkids.getLength(); k++) {
                        Node gkid = gkids.item(k);
                        log.info("gkid " +gkid.getNodeName() + " :: " +  gkid.getNodeValue());
                        lastQuote.setAuthor(gkid.getNodeValue());
                        log.info("lastQuote " + lastQuote);
                    }
                }
			}
			    
		}

		if ("dd".equals(name)) {
			if (lastDt != null && lastDt.equals(n.getPreviousSibling())) {
				sb.append("found author");
				lastDD = n;
			}
		}

		if ("#text".equals(n.getNodeName())) {
			Node parent = n.getParentNode();
			if (lastDD != null
					&& parentOrGrandParent(lastDD, n)
					&& ("b".equals(parent.getNodeName()) || "i".equals(parent
							.getNodeName()))) {
				String txt = n.getNodeValue();
				if (txt.length() > 2) {
					if (lastQuote.getAuthor() == null) {
						lastQuote.setAuthor(n.getNodeValue());
					} else {
						lastQuote.setAttribution(n.getNodeValue());
						if(!dupQ(lastQuote)) {
							quotes.add(lastQuote);
						}
						lastQuote = null;
					}
				}
			}
		}

		NodeList nl = n.getChildNodes();
		for (int j = 0; j < nl.getLength(); j++) {
			sb.append(parseNode(i + 1, nl.item(j)));
		}
		return sb.toString();
	}
}
