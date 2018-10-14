package clock

import io._
import spinal.core._

class ClockWizard(numClocks: Int = 1,
                  divs: Array[Double] = Array(1),
                  masterDiv: Int = 1,
                  masterMult: Double = 1,
                  clkInPeriod: Double) extends Component {
  val io = new Bundle {
    val clkIn = in Bool
    val clkOut = out Vec(Bool, numClocks)
    val locked = out Bool
  }

  assert(numClocks >= 1 && numClocks <= 6)
  assert(divs.length == numClocks)
  for (idx <- 1 until numClocks) {
    // Divs 1..6 must be integers
    assert((divs(idx) - divs(idx).floor).abs < 0.001)
  }

  val mmcme2 = new Mmcme2Base(
    div0 = divs(0),
    div1 = if (numClocks >= 2) divs(1).toInt else 1,
    div2 = if (numClocks >= 3) divs(2).toInt else 1,
    div3 = if (numClocks >= 4) divs(3).toInt else 1,
    div4 = if (numClocks >= 5) divs(4).toInt else 1,
    div5 = if (numClocks == 6) divs(5).toInt else 1,
    masterDiv = masterDiv,
    masterMult = masterMult,
    clkInPeriod = clkInPeriod
  )

  // Input via IBUF, no RESET.
  mmcme2.io.CLKIN1 := IBufG(io.clkIn)
  mmcme2.io.RST := False

  // Feedback loop via BUFG.
  mmcme2.io.CLKFBIN := BufG(mmcme2.io.CLKFBOUT)

  // Output via BUFGs
  io.clkOut(0) := BufG(mmcme2.io.CLKOUT0)
  if (numClocks >= 2) io.clkOut(1) := BufG(mmcme2.io.CLKOUT1)
  if (numClocks >= 3) io.clkOut(2) := BufG(mmcme2.io.CLKOUT2)
  if (numClocks >= 4) io.clkOut(3) := BufG(mmcme2.io.CLKOUT3)
  if (numClocks >= 5) io.clkOut(4) := BufG(mmcme2.io.CLKOUT4)
  if (numClocks == 6) io.clkOut(5) := BufG(mmcme2.io.CLKOUT5)

  // Locked flag
  io.locked := mmcme2.io.LOCKED
}
