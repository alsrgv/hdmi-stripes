package io

import spinal.core._

class BufR(divide: Int) extends BlackBox {
  val generic = new Generic {
    val BUFR_DIVIDE = if (divide > 0) divide.toString else "BYPASS"
  }

  val io = new Bundle {
    val I = in Bool
    val CE = in Bool
    val CLR = in Bool
    val O = out Bool
  }

  noIoPrefix()
  setBlackBoxName("BUFR")
}

object BufR {
  def apply(signal: Bool, divide: Int): Bool = {
    val bufR = new BufR(divide)
    bufR.io.I := signal
    bufR.io.CE := True
    bufR.io.CLR := False
    bufR.io.O
  }
}