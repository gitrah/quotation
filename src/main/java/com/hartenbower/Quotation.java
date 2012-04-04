package com.hartenbower;

import java.io.Serializable;

public class Quotation implements Serializable {
    private static final long serialVersionUID = 1L;
    public final static String SER_BIN = "quotations.ser";
    public final static String SER_XML = "quotations.xml";
    public final static String SER_IDS = "quotations.ids";
    public final static String SER = SER_BIN;

    public static Quotation NO_QUOTE = new Quotation("nobody", "nothing");
    private String uuid;
	private String author;
    
    private String attribution;

    private String body;
    
    public Quotation() {}
    public Quotation(String author, String body) {
        this.author=author;
        this.body=body;
    }
    
    public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
 
    public static boolean nullEmptyQ(String s) {
        return s == null || "".equals(s);
    }

    @Override
    public String toString() {
        return body + " -- " + (!nullEmptyQ(attribution) ? attribution + " -- " : "") + author;  
    }
    
    public String toSearchString() {
    	StringBuffer sb = new StringBuffer(body);
    	sb.append("\n");
    	sb.append(author);
    	sb.append("\n");
    	sb.append(attribution);
        return sb.toString();  
    }
    
    public String toSearchString(int idx, int len) {
        String s = toSearchString();
        String part = s.substring(idx,len+idx);
        return s.substring(0, idx) + part.toUpperCase() + s.substring(len+idx);
    }
    
    public String toHtml() {
        return body + "<br/> -- " + (!nullEmptyQ(attribution) ? attribution + " -- " : "") + author + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/adm/quotations.jsp\">search</a>";
    }
    
}
