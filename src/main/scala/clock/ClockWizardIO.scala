package clock

import io._
import spinal.core._

class ClockWizardIO(numClocks: Int = 1,
                    div0: Double = 1,
                    divsOnTop: Array[Int] = Array(),
                    masterDiv: Int = 1,
                    masterMult: Double = 1,
                    clkInPeriod: Double) extends Component {
  val io = new Bundle {
    val clkIn = in Bool
    val clkOut = out Vec(Bool, numClocks)
    val locked = out Bool
  }

  assert(numClocks >= 1 && numClocks <= 6)
  assert(divsOnTop.length == numClocks - 1)

  val mmcme2 = new Mmcme2Base(
    div0 = div0,
    masterDiv = masterDiv,
    masterMult = masterMult,
    clkInPeriod = clkInPeriod
  )

  // Input via IBUF, no RESET.
  mmcme2.io.CLKIN1 := IBufG(io.clkIn)
  mmcme2.io.RST := False

  // Feedback loop via BUFR.
  mmcme2.io.CLKFBIN := BufR(mmcme2.io.CLKFBOUT, divide = 1)

  // Output via BUFIO + BUFRs.
  io.clkOut(0) := BufIO(mmcme2.io.CLKOUT0)
  if (numClocks >= 2) io.clkOut(1) := BufR(mmcme2.io.CLKOUT0, divide = divsOnTop(0))
  if (numClocks >= 3) io.clkOut(2) := BufR(mmcme2.io.CLKOUT0, divide = divsOnTop(1))
  if (numClocks >= 4) io.clkOut(3) := BufR(mmcme2.io.CLKOUT0, divide = divsOnTop(2))
  if (numClocks >= 5) io.clkOut(4) := BufR(mmcme2.io.CLKOUT0, divide = divsOnTop(3))
  if (numClocks == 6) io.clkOut(5) := BufR(mmcme2.io.CLKOUT0, divide = divsOnTop(4))

  // Locked flag
  io.locked := mmcme2.io.LOCKED
}
