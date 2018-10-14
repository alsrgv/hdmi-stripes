package hdmi

import spinal.core._
import spinal.core.sim._
import tmds.TmdsEncoder

class SimpleDriver extends Component {
  val io = new Bundle {
    val r_tmds = out Bits(10 bits)
    val g_tmds = out Bits(10 bits)
    val b_tmds = out Bits(10 bits)
  }

  val x = Reg(UInt(10 bits)) init 0
  val y = Reg(UInt(10 bits)) init 0
  val r_enc = new TmdsEncoder
  val g_enc = new TmdsEncoder
  val b_enc = new TmdsEncoder

  when (x === 799) {
    x := 0
    y := (y < 524) ? (y + 1) | 0
  } otherwise {
    x := x + 1
  }

  val hSync = (x >= 656) && (x < 752)
  val vSync = (y >= 490) && (y < 492)
  val drawArea = (x < 640) && (y < 480)

  r_enc.io.disp_ena := drawArea
  r_enc.io.control := 0
  r_enc.io.d_in := (x(0 to 5).asBits & B(6 bits, default -> (y(3 to 4) =/= ~x(3 to 4)))) ## B"00"
  g_enc.io.disp_ena := drawArea
  g_enc.io.control := 0
  g_enc.io.d_in := x(0 to 7).asBits & B(8 bits, default -> y(6))
  b_enc.io.disp_ena := drawArea
  b_enc.io.control := vSync ## hSync
  b_enc.io.d_in := y(0 to 7).asBits

  io.r_tmds := RegNext(r_enc.io.d_out) init 0
  io.g_tmds := RegNext(g_enc.io.d_out) init 0
  io.b_tmds := RegNext(b_enc.io.d_out) init 0
}

object SimpleDriverSim {
  def main(args: Array[String]): Unit = {
    val compiled = SimConfig.withWave.compile {
      val dut = new SimpleDriver
      dut
    }

    compiled.doSim { dut =>
      //Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      var idx = 0
      while (idx < 10000) {
        dut.clockDomain.waitSampling()
        idx += 1
      }
    }
  }
}
