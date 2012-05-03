package com.hartenbower

import scala.collection.JavaConverters._
import org.apache.log4j.Logger

object QuoteWorker {
  val log = Logger.getLogger("QuoteWorker")
  var quotes : List[Quotation] = _

  def loadQuotations {
	quotes = List[Quotation]() ++ FileUtil.fromFile("./quotations.ser", classOf[Quotation]).asInstanceOf[java.util.List[Quotation]].asScala
  }
  
  def setQuotations(jquotes : java.util.List[Quotation]) {
    quotes = List[Quotation]() ++ jquotes.asScala
  }
  
  private def searcher(s: String, l: Int)(indexTuple : Tuple2[Int,Int] ) : Option[List[Tuple2[Int,Int]]] = {
    var resultList : Option[List[Tuple2[Int,Int]]] = None 
    for(i <- indexTuple._1 to indexTuple._2) {
       val q = quotes(i)
       val res = Qutil.ldIndexOf(q.toSearchString, s, l)
       if(res._2 > -1) {
         resultList match {
           case Some(list) =>
             resultList = Some(list :+ Tuple2[Int,Int](i,res._2))
           case None=>
             resultList = Some(List[Tuple2[Int,Int]]() :+ Tuple2[Int,Int](i,res._2))
         }
       }
    }
    resultList match {
      case Some(list) =>
        log.info("found " + list.size + " in " + indexTuple)
      case _ =>
    }
    resultList
  }
  
  def search(s:String) {
    search(s, 5)
  }
  
  def search(s:String, lev: Int) {
    val lock = new AnyRef()
    val scheduler = new Scheduler[Quotation](lock,quotes)
    scheduler.start
    scheduler ! Init(searcher(s,lev))
    lock.synchronized {
    	lock.wait  
    }
    

    for (i <- scheduler.results) {
      System.out.println(scheduler.queue(i._1).toSearchString(i._2, s.length))
    }

  }
}