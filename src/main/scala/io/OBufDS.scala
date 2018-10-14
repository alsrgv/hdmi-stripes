package io

import spinal.core._

object Slew extends Enumeration {
  val SLOW, FAST = Value
}

object IOStandard extends Enumeration {
  val DEFAULT, TMDS_33, LVDS_25 = Value
}

class OBufDS(ioStandard: IOStandard.Value = IOStandard.DEFAULT,
             slew: Slew.Value = Slew.SLOW) extends BlackBox {
  val generic = new Generic {
    val IOSTANDARD = ioStandard.toString
    val SLEW = slew.toString
  }

  val io = new Bundle {
    val I = in Bool
    val O = out Bool
    val OB = out Bool
  }

  noIoPrefix()
  setBlackBoxName("OBUFDS")
}

object OBufDS {
  def apply(signal: Bool,
            p: Bool,
            n: Bool,
            ioStandard: IOStandard.Value = IOStandard.DEFAULT,
            slew: Slew.Value = Slew.SLOW) = {
    val oBufDS = new OBufDS(ioStandard, slew)
    oBufDS.io.I := signal
    p := oBufDS.io.O
    n := oBufDS.io.OB
  }
}