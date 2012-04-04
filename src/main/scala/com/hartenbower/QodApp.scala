package com.hartenbower

import scala.collection.JavaConverters._
import java.util.Random
import scala.swing._
import scala.swing.event._
import GridBagPanel._

import org.apache.log4j.Logger

object QodApp extends SimpleSwingApplication {
  val log = Logger.getLogger("QodApp")
  val qods = List[Quotation]() ++ Qutil.qods.asScala
  val total = qods.size
  val rnd = new Random(System.currentTimeMillis)
  
  val bye = new Button {
    text = "Exit"
  }
  val next = new Button {
    text = "Next"
  }
  val pause = new Button {
    text = "Pause"
  }
  val theFont = new Font("Arial", 0, 12).deriveFont(java.awt.Font.BOLD)
  var qod = new TextArea(10,60) {
    val dim = new java.awt.Dimension(10,60)
    font = theFont
    lineWrap = true
    enabled = false
    maximumSize = dim
    var running = false
    var paused = false
        val t = new Thread {
            override def run {
                running = true
                while (running) {
                  if(!paused) text = "" +  qods(rnd.nextInt(total))
                  Thread.sleep(30000)
                }
            }
        }
        t.start
  }
  
  def top = new MainFrame {
    title = "QOD"

    contents = new BoxPanel(Orientation.Vertical) {
      contents += new FlowPanel {
        contents += qod
        contents += next
        contents += pause
        contents += bye
      }
    }

    listenTo( bye,next,pause)
    var nClicks = 0
    reactions += {
      case ButtonClicked(btn) ⇒
        btn match {
          case b if btn == bye ⇒
            qod.running = false
            System.exit(0)
          case b if btn == next ⇒
            qod.text = "" + qods(rnd.nextInt(total))
         case b if btn == pause ⇒
            qod.paused = !qod.paused
            pause.text = if(qod.paused) "Resume" else "Pause"
        }
    }
  }

}