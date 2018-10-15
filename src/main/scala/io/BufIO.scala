package io

import spinal.core._

class BufIO extends BlackBox {
  val io = new Bundle {
    val I = in Bool
    val O = out Bool
  }

  noIoPrefix()
  setBlackBoxName("BUFIO")
}

object BufIO {
  def apply(signal: Bool): Bool = {
    val bufIO = new BufIO
    bufIO.io.I := signal
    bufIO.io.O
  }
}