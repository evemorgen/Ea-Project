import scala.util.Random

object Main extends App {

  type SeqEnergy = (Double, Seq[Int])

  def autocorr(seq: Seq[Int], k: Int): Double = {
    (0 until seq.length - k)
      .map(i => seq(i) * seq(i + k))
      .sum
  }

  def skewEnergy(seq: Seq[Int]): Double = {
    energy(generateSkewSymmetry(seq), 2)
  }

  def energy(seq: Seq[Int], step: Int = 1): Double = {
    (1 until seq.length by step)
      .map { i =>
        val corr = autocorr(seq, i)
        corr * corr
      }
      .sum
  }

  def generateSkewSymmetry(elements: Seq[Int]): Seq[Int] = {
    elements ++ elements.dropRight(1).zipWithIndex.map {
      case (element, index) if index % 2 == 0 => element
      case (element, _) => -element
    }.reverse
  }

  def allSeqs(elements: Seq[Int], len: Int): Seq[Seq[Int]] = {
    (1 to len)
      .map(_ => elements)
      .foldLeft(Seq(Seq.empty[Int])) {
        (x, y) => for (a <- x.view; b <- y) yield a :+ b
      }
  }

  def randomSeq(seqs: Seq[Seq[Int]]): Seq[Int] = Random.shuffle(seqs).head

  def neighbours(seq: Seq[Int]): Seq[Seq[Int]] = (0 until seq.length).map(i => seq.updated(i, seq(i) * -1))

  def run(path: List[Seq[Int]], iter: Int): List[Seq[Int]] = {
    if (iter > 10 || skewEnergy(path.last) < 10)
      path
    else {
      val ngbh = neighbours(path.last).filter(n => !path.contains(n)).map(
        n => (skewEnergy(n), n))
      if (ngbh.isEmpty) {
        path
      } else {
        val (_, bestNeighbour) = ngbh.minBy(_._1)
        run(path :+ bestNeighbour, iter + 1)
      }

    }
  }

  def selfAvoidingWalk(n: Int): SeqEnergy = {
    val intialSequence = randomSeq(allSeqs(Seq(1, -1), n))
    val path = List(intialSequence)
    run(path, 1000)
      .map(generateSkewSymmetry)
      .map(seq => (energy(seq), seq))
      .minBy(_._1)
  }

  def workFor(n: Int, start: Long, bestPaths: List[SeqEnergy]): SeqEnergy = {
    start.toLong + n*1000 compare System.currentTimeMillis() match {
      case 0   => bestPaths.minBy(_._1)
      case -1  => bestPaths.minBy(_._1)
      case 1 => {
        if (System.currentTimeMillis() % 5000 < 300) {
          val (bestEnergy, bestSequence) = if (bestPaths.nonEmpty) bestPaths.minBy(_._1) else (99999999, Seq())
          val meritFactor = scala.math.pow(bestSequence.length, 2) / (2 * bestEnergy.asInstanceOf[Double])
          val now = System.currentTimeMillis()
          println(s"Merit Factor: $meritFactor, Energy: $bestEnergy, Time: $now")
        } else ()
        workFor(n, start, bestPaths :+ selfAvoidingWalk(21))
      }
    }
  }

  def workFor(n: Int): (Double, Seq[Int]) = workFor(n, System.currentTimeMillis(), List())
 /*
  println(energy(Seq(1, -1, 1, -1, 1, -1, 1, -1))) //140
  println(energy(Seq(1, -1, 1, -1, 1, -1, 1, 1))) //56
  println(energy(Seq(1, -1, 1, -1, 1, -1, -1, 1))) //36
  println(energy(Seq(1, 1, 1, -1, 1, -1, -1, 1))) //8
  */
  workFor(600) // 10 minutes
}

