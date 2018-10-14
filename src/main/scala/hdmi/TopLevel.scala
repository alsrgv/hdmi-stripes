package hdmi

import clock.ClockWizard
import io._
import spinal.core._

class TopLevel extends Component {
  val io = new Bundle {
    val hdmi_tx_clk_p = out Bool
    val hdmi_tx_clk_n = out Bool
    val hdmi_tx_p = out Bits(3 bits)
    val hdmi_tx_n = out Bits(3 bits)
  }

  noIoPrefix()

  val clkCtrl = new Area {
    // 125MHz -> 250MHz, 25MHz
    val clkWiz = new ClockWizard(2, divs = Array(8, 40), masterMult = 8, clkInPeriod = 8.0)
    clkWiz.io.clkIn := clockDomain.readClockWire
    val reset = clockDomain.readResetWire || !clkWiz.io.locked

    val hdmiClockDomain = ClockDomain.internal("hdmi")
    hdmiClockDomain.clock := clkWiz.io.clkOut(0)
    hdmiClockDomain.reset := reset

    val pixelClockDomain = ClockDomain.internal("pixel")
    pixelClockDomain.clock := clkWiz.io.clkOut(1)
    pixelClockDomain.reset := reset

    // rising edge is synchronized
    pixelClockDomain.setSyncronousWith(hdmiClockDomain)
  }

  val driverCore = new ClockingArea(clkCtrl.pixelClockDomain) {
    val driver = new SimpleDriver
    OBufDS(ClockDomain.current.readClockWire,
      io.hdmi_tx_clk_p, io.hdmi_tx_clk_n,
      ioStandard = IOStandard.TMDS_33, slew = Slew.FAST)
  }

  val hdmiCore = new ClockingArea(clkCtrl.hdmiClockDomain) {
    OBufDS(
      OSerDes10(driverCore.driver.io.r_tmds, clkCtrl.pixelClockDomain),
      io.hdmi_tx_p(2), io.hdmi_tx_n(2),
      ioStandard = IOStandard.TMDS_33, slew = Slew.FAST)
    OBufDS(
      OSerDes10(driverCore.driver.io.g_tmds, clkCtrl.pixelClockDomain),
      io.hdmi_tx_p(1), io.hdmi_tx_n(1),
      ioStandard = IOStandard.TMDS_33, slew = Slew.FAST)
    OBufDS(
      OSerDes10(driverCore.driver.io.b_tmds, clkCtrl.pixelClockDomain),
      io.hdmi_tx_p(0), io.hdmi_tx_n(0),
      ioStandard = IOStandard.TMDS_33, slew = Slew.FAST)

//    OBufDS(ParallelToSerial(driverCore.driver.io.r_tmds),
//      io.hdmi_tx_p(2), io.hdmi_tx_n(2),
//      ioStandard = IOStandard.TMDS_33, slew = Slew.FAST)
//    OBufDS(ParallelToSerial(driverCore.driver.io.g_tmds),
//      io.hdmi_tx_p(1), io.hdmi_tx_n(1),
//      ioStandard = IOStandard.TMDS_33, slew = Slew.FAST)
//    OBufDS(ParallelToSerial(driverCore.driver.io.b_tmds),
//      io.hdmi_tx_p(0), io.hdmi_tx_n(0),
//      ioStandard = IOStandard.TMDS_33, slew = Slew.FAST)
  }
}

object TopLevelVerilog {
  def main(args: Array[String]) {
    val config = new SpinalConfig {
      override val netlistFileName = "top_level.v"
    }
    // It's really important to have TopLevel constructions inside the call.
    // Otherwise, in case of error all sorts of weird things happen.
    SpinalVerilog(config)(new TopLevel)
  }
}
