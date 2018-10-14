package io

import spinal.core._

object DataRate extends Enumeration {
  val SDR, DDR = Value
}

object SerDesMode extends Enumeration {
  val MASTER, SLAVE = Value
}

class OSerDese2(dataRate: DataRate.Value = DataRate.DDR,
                dataWidth: Int = 4,
                serDesMode: SerDesMode.Value = SerDesMode.MASTER) extends BlackBox {
  val generic = new Generic {
    val DATA_RATE_OQ = dataRate.toString
    val DATA_RATE_TQ = DataRate.SDR.toString
    val DATA_WIDTH = dataWidth
    val TRISTATE_WIDTH = 1
    val SERDES_MODE = serDesMode.toString
  }

  val io = new Bundle {
    val CLK = in Bool
    val CLKDIV = in Bool
    val D1 = in Bool
    val D2 = in Bool
    val D3 = in Bool
    val D4 = in Bool
    val D5 = in Bool
    val D6 = in Bool
    val D7 = in Bool
    val D8 = in Bool
    val OCE = in Bool
    val RST = in Bool
    val SHIFTIN1 = in Bool
    val SHIFTIN2 = in Bool

    val OQ = out Bool
    val SHIFTOUT1 = out Bool
    val SHIFTOUT2 = out Bool
  }

  noIoPrefix()
  setBlackBoxName("OSERDESE2")
}

object OSerDes10 {
  def apply(data: Bits, slowClockDomain: ClockDomain): Bool = {
    assert(data.getWidth == 10)
    val master = new OSerDese2(dataWidth = 10, serDesMode = SerDesMode.MASTER)
    val slave = new OSerDese2(dataWidth = 10, serDesMode = SerDesMode.SLAVE)

    // Reset deassert must be synchronized with CLKDIV
    val rstReg = RegNext(slowClockDomain.readResetWire) init True
    master.io.CLK := ClockDomain.current.readClockWire
    master.io.CLKDIV := slowClockDomain.readClockWire
    master.io.RST := rstReg
    master.io.OCE := True
    slave.io.CLK := ClockDomain.current.readClockWire
    slave.io.CLKDIV := slowClockDomain.readClockWire
    slave.io.RST := rstReg
    slave.io.OCE := True

    master.io.D1 := data(0)
    master.io.D2 := data(1)
    master.io.D3 := data(2)
    master.io.D4 := data(3)
    master.io.D5 := data(4)
    master.io.D6 := data(5)
    master.io.D7 := data(6)
    master.io.D8 := data(7)
    slave.io.D1 := False
    slave.io.D2 := False
    slave.io.D3 := data(8)
    slave.io.D4 := data(9)
    slave.io.D5 := False
    slave.io.D6 := False
    slave.io.D7 := False
    slave.io.D8 := False

    master.io.SHIFTIN1 := slave.io.SHIFTOUT1
    master.io.SHIFTIN2 := slave.io.SHIFTOUT2
    slave.io.SHIFTIN1 := False
    slave.io.SHIFTIN2 := False

    master.io.OQ
  }
}