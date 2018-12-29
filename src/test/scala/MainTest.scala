import com.typesafe.config.Config
import org.scalatest.FunSuite
import org.scalatest.mockito._
import org.mockito.Mockito._


class MainTest extends FunSuite with MockitoSugar {
  test("Energy is calculated properly") {
    assert(Main.energy(Seq(1, -1, 1, -1, 1, -1, 1, -1)) === 140)
    assert(Main.energy(Seq(1, -1, 1, -1, 1, -1, 1, 1)) === 56)
    assert(Main.energy(Seq(1, -1, 1, -1, 1, -1, -1, 1)) === 36)
    assert(Main.energy(Seq(1, 1, 1, -1, 1, -1, -1, 1)) === 8)

    //pdf examples
    assert(Main.energy(Seq(1, 1, 1, -1, 1, -1, -1, 1)) === Main.energy(Seq(-1, 1, -1, -1, -1, -1, 1, 1)))
    assert(Main.energy(Seq(1, 1, 1, -1, 1, -1, -1, 1)) === Main.energy(Seq(1, -1, 1, 1, 1, 1, -1, -1)))

    //results examples
    assert(Main.energy(Seq(-1, -1, 1, 1, 1, 1, 1, -1, -1, 1, 1, -1, 1, -1, 1, 1, -1)) === 32)
  }

  test("Skew sequence generation works") {
    assert(
      Main.generateSkewSymmetry(Seq(-1, -1, -1, -1, -1, 1, 1)) ===
      Seq(-1, -1, -1, -1, -1, 1, 1, -1, -1, 1, -1, 1, -1)
    )

    assert(
      Main.generateSkewSymmetry(Seq(-1, -1, -1, -1, -1, 1, 1)).length ===
      (Seq(-1, -1, -1, -1, -1, 1, 1).length * 2 - 1)
    )

    assert(
      Main.energy(Seq(-1, -1, -1, -1, -1, 1, 1, -1, -1, 1, -1, 1, -1)) ===
        Main.skewEnergy(Seq(-1, -1, -1, -1, -1, 1, 1))
    )
  }

  test("Random sequence generation gives n length seq") {
    assert(Main.randomSeq(5).length === 5)
    assert(Main.randomSeq(10).length === 10)
  }

  test("Neighbour sequences generation works") {
    assert(
      Main.neighbours(Seq(1, 1, 1)) === Seq(
        Seq(-1, 1, 1),
        Seq(1, -1, 1),
        Seq(1, 1, -1)
      )
    )
  }

  test("run ends when iteration number exceeded or energy threshold reached") {
    val initialSequence = Seq(1, 1, 1)
    val config1 = mock[Config]
    when(config1.getInt("iterationsNumber")).thenReturn(1)
    when(config1.getInt("energyThreshold")).thenReturn(0)
    val config2 = mock[Config]
    when(config2.getInt("iterationsNumber")).thenReturn(10)
    when(config2.getInt("energyThreshold")).thenReturn(9999)

    // iteration number exceeded
    val res1 = Main.run(List(initialSequence), 2, config1)
    assert(res1 === List(Seq(1, 1, 1)))

    // energy has fallen below threshold
    val res2 = Main.run(List(initialSequence), 2, config2)
    assert(res2 === List(Seq(1, 1, 1)))
  }

  test("result after 1 iteration is path with best neighbour") {
    val initialSequence = Seq(1, 1, 1)
    val config = mock[Config]
    when(config.getInt("iterationsNumber")).thenReturn(1)
    when(config.getInt("energyThreshold")).thenReturn(0)


    val res = Main.run(List(initialSequence), 1, config)
    assert(res === List(Seq(1, 1, 1), Seq(-1, 1, 1)))
  }
}

