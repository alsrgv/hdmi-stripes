package tmds

import spinal.core._
import spinal.core.sim._

import scala.util.Random

class TmdsTest extends Component {
  val io = new Bundle {
    val disp_ena_in = in Bool
    val disp_ena_out = out Bool
    val control_in = in Bits (2 bits)
    val control_out = out Bits (2 bits)
    val d_in = in Bits (8 bits)
    val d_out = out Bits (8 bits)
  }

  val enc = new TmdsEncoder
  val dec = new TmdsDecoder
  enc.io.disp_ena := io.disp_ena_in
  enc.io.control := io.control_in
  enc.io.d_in := io.d_in
  dec.io.d_in := enc.io.d_out
  io.disp_ena_out := dec.io.disp_ena
  io.control_out := dec.io.control
  io.d_out := dec.io.d_out
}

object TmdsTestVerilog {
  def main(args: Array[String]) {
    // It's really important to have TopLevel constructions inside the call.
    // Otherwise, in case of error all sorts of weird things happen.
    SpinalVerilog(new TmdsTest)
  }
}

object TmdsTestSim {
  def main(args: Array[String]): Unit = {
    val compiled = SimConfig.withWave.compile {
      val dut = new TmdsTest
      dut.enc.disparity.simPublic()
      dut
    }

    compiled.doSim { dut =>
      //Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      var idx = 0
      while (idx < 512) {
        dut.io.disp_ena_in #= true
        dut.io.control_in #= Random.nextInt(4)
        dut.io.d_in #= idx % 256

        dut.clockDomain.waitSampling()
        assert(dut.io.disp_ena_out.toBoolean)
        assert(8 >= dut.enc.disparity.toInt)
        assert(-8 <= dut.enc.disparity.toInt)
        assert(0 == dut.io.control_out.toInt)
        assert(idx % 256 == dut.io.d_out.toInt)
        idx += 1
      }

      while (idx < 1024) {
        dut.io.disp_ena_in #= true
        dut.io.control_in #= Random.nextInt(4)
        dut.io.d_in #= Random.nextInt(256)

        dut.clockDomain.waitSampling()
        assert(dut.io.disp_ena_out.toBoolean)
        assert(8 >= dut.enc.disparity.toInt)
        assert(-8 <= dut.enc.disparity.toInt)
        assert(0 == dut.io.control_out.toInt)
        assert(dut.io.d_in.toInt == dut.io.d_out.toInt)
        idx += 1
      }

      while (idx < 1536) {
        dut.io.disp_ena_in #= false
        dut.io.control_in #= idx % 4
        dut.io.d_in #= Random.nextInt(256)

        dut.clockDomain.waitSampling()
        assert(!dut.io.disp_ena_out.toBoolean)
        assert(0 == dut.enc.disparity.toInt)
        assert(idx % 4 == dut.io.control_out.toInt)
        assert(0 == dut.io.d_out.toInt)
        idx += 1
      }
    }
  }
}
