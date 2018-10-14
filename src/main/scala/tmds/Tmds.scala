package tmds

import spinal.core._

class TmdsEncoder extends Component {
  val io = new Bundle {
    val disp_ena = in Bool
    val control = in Bits (2 bits)
    val d_in = in Bits (8 bits)
    val d_out = out Bits (10 bits)
  }

  // This is a register used to keep track of the data stream disparity.
  // A positive value represents the excess number of 1s that have been
  // transmitted. A negative value represents the excess number of 0s
  // that have been transmitted.
  val disparity = Reg(SInt(5 bits)) init (0)

  when(io.disp_ena) {
    val ones_d_in = TmdsUtils.countOnes(io.d_in)
    val use_xor = ones_d_in < 4 || ones_d_in === 4 && io.d_in(0)
    val q_m = use_xor ?
      TmdsUtils.rollingXor(io.d_in, withResult = true) |
      TmdsUtils.rollingXNor(io.d_in, withResult = true)

    val ones_q_m = TmdsUtils.countOnes(q_m)
    val diff_q_m = ones_q_m.asSInt - (8 - ones_q_m.asSInt)
    val inv_q_m = Bool()

    when(disparity === 0 || ones_q_m === 4) {
      // inv negates xor to preserve the balance between ones and zeroes.
      inv_q_m := ~use_xor
    } otherwise {
      inv_q_m := disparity > 0 && ones_q_m > 4 || disparity < 0 && ones_q_m < 4
    }

    // Output.
    io.d_out := inv_q_m ## use_xor ## (inv_q_m ? ~q_m | q_m)

    // Adjust disparity.
    disparity := disparity + (inv_q_m ? -diff_q_m | diff_q_m) +
      (inv_q_m ? S"d1" | S"d-1") + (use_xor ? S"d1" | S"d-1")

  } otherwise {
    // Output.
    io.d_out := io.control.mux(
      B"00" -> B(TmdsUtils.CTRL_00),
      B"01" -> B(TmdsUtils.CTRL_01),
      B"10" -> B(TmdsUtils.CTRL_10),
      B"11" -> B(TmdsUtils.CTRL_11)
    )

    // Adjust disparity.
    disparity := 0
  }
}

class TmdsDecoder extends Component {
  val io = new Bundle {
    val disp_ena = out Bool
    val control = out Bits (2 bits)
    val d_in = in Bits (10 bits)
    val d_out = out Bits (8 bits)
  }

  // Default to disabled display.
  io.disp_ena := False
  io.d_out := 0

  when (io.d_in === B(TmdsUtils.CTRL_00)) {
    io.control := B"00"
  } elsewhen (io.d_in === B(TmdsUtils.CTRL_01)) {
    io.control := B"01"
  } elsewhen (io.d_in === B(TmdsUtils.CTRL_10)) {
    io.control := B"10"
  } elsewhen (io.d_in === B(TmdsUtils.CTRL_11)) {
    io.control := B"11"
  } otherwise {
    io.disp_ena := True
    io.control := 0
    val q_m = io.d_in(9) ? ~io.d_in(0 to 7) | io.d_in(0 to 7)
    io.d_out := io.d_in(8) ?
      TmdsUtils.rollingXor(q_m, withResult = false) |
      TmdsUtils.rollingXNor(q_m, withResult = false)
  }
}

private object TmdsUtils {
  def countOnes(vec: Bits): UInt = {
    var result = U(0, log2Up(vec.getBitsWidth + 1) bits)
    for (bit <- 0 until vec.getBitsWidth) {
      result = result + vec(bit).asUInt
    }
    result
  }

  def rollingXor(vec: Bits, withResult: Boolean): Bits = {
    val result = Vec(Bool, vec.getBitsWidth)
    result(0) := vec(0)
    for (bit <- 1 until vec.getBitsWidth) {
      val xorWith = if (withResult) result(bit - 1) else vec(bit - 1)
      result(bit) := vec(bit) ^ xorWith
    }
    result.asBits
  }

  def rollingXNor(vec: Bits, withResult: Boolean): Bits = {
    val result = Vec(Bool, vec.getBitsWidth)
    result(0) := vec(0)
    for (bit <- 1 until vec.getBitsWidth) {
      val xnorWith = if (withResult) result(bit - 1) else vec(bit - 1)
      result(bit) := vec(bit) === xnorWith
    }
    result.asBits
  }

  val CTRL_00 = "1101010100"
  val CTRL_01 = "0010101011"
  val CTRL_10 = "0101010100"
  val CTRL_11 = "1010101011"
}
