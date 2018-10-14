package io

import spinal.core._
import spinal.core.sim._

import scala.util.Random

class ParallelToSerialTest extends Component {
  val io = new Bundle {
    val input = in Bits(10 bits)
    val output = out Bool
  }

  val p2s = new ParallelToSerial(10)
  p2s.io.input := io.input
  io.output := p2s.io.output
}

object ParallelToSerialTestVerilog {
  def main(args: Array[String]) {
    // It's really important to have TopLevel constructions inside the call.
    // Otherwise, in case of error all sorts of weird things happen.
    SpinalVerilog(new ParallelToSerialTest)
  }
}

object ParallelToSerialTestSim {
  def main(args: Array[String]): Unit = {
    SimConfig.withWave.compile(new ParallelToSerialTest).doSim { dut =>
      //Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      var idx = 0
      var currentInput = 0
      var nextInput = 0
      while (idx < 100) {
        if (idx % 10 == 0) {
          currentInput = nextInput
          nextInput = Random.nextInt(1024)
          dut.io.input #= nextInput
        }

        dut.clockDomain.waitSampling()
        if (idx >= 10) {
          assert((currentInput % 2 == 1) == dut.io.output.toBoolean)
          currentInput >>= 1
        }
        idx += 1
      }
    }
  }
}
