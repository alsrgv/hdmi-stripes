package io

import spinal.core._

class IBufG extends BlackBox {
  val io = new Bundle {
    val I = in Bool
    val O = out Bool
  }

  noIoPrefix()
  setBlackBoxName("IBUFG")
}

object IBufG {
  def apply(signal: Bool): Bool = {
    val iBufG = new IBufG
    iBufG.io.I := signal
    iBufG.io.O
  }
}