package clock

import spinal.core._

class Mmcme2Base(div0: Double = 1,
                 div1: Int = 1,
                 div2: Int = 1,
                 div3: Int = 1,
                 div4: Int = 1,
                 div5: Int = 1,
                 masterDiv: Int = 1,
                 masterMult: Double = 1,
                 clkInPeriod: Double) extends BlackBox {
  val generic = new Generic {
    val CLKOUT0_DIVIDE_F = div0
    val CLKOUT1_DIVIDE = div1
    val CLKOUT2_DIVIDE = div2
    val CLKOUT3_DIVIDE = div3
    val CLKOUT4_DIVIDE = div4
    val CLKOUT5_DIVIDE = div5
    val DIVCLK_DIVIDE = masterDiv
    val CLKFBOUT_MULT_F = masterMult
    val CLKIN1_PERIOD = clkInPeriod
  }

  val io = new Bundle {
    val CLKIN1 = in Bool
    val CLKFBIN = in Bool
    val RST = in Bool

    val CLKOUT0 = out Bool
    val CLKOUT1 = out Bool
    val CLKOUT2 = out Bool
    val CLKOUT3 = out Bool
    val CLKOUT4 = out Bool
    val CLKOUT5 = out Bool
    val CLKFBOUT = out Bool
    val LOCKED = out Bool
  }

  noIoPrefix()
  setBlackBoxName("MMCME2_BASE")
}
