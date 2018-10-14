package io

import spinal.core._

class BufG extends BlackBox {
  val io = new Bundle {
    val I = in Bool
    val O = out Bool
  }

  noIoPrefix()
  setBlackBoxName("BUFG")
}

object BufG {
  def apply(signal: Bool): Bool = {
    val bufG = new BufG
    bufG.io.I := signal
    bufG.io.O
  }
}