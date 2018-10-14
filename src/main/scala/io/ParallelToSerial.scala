package io

import spinal.core._

class ParallelToSerial(width: Int) extends Component {
  val io = new Bundle {
    val input = in Bits(width bits)
    val output = out Bool
  }

  val cnt = Reg(UInt(log2Up(width) bits)) init 0
  val shadow = Reg(Bits(width * 2 - 1 bits)) init 0

  when (cnt === 0) {
    shadow := io.input ## shadow(1 until width)
  } otherwise {
    shadow := B"0" ## shadow(1 until shadow.getWidth)
  }
  io.output := shadow(0)

  when (cnt < width - 1) {
    cnt := cnt + 1
  } otherwise {
    cnt := 0
  }
}

object ParallelToSerial {
  def apply(input: Bits): Bool = {
    val p2s = new ParallelToSerial(input.getWidth)
    p2s.io.input := input
    p2s.io.output
  }
}