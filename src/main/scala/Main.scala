import scala.util.Random

object Main extends App {

  def autocorr(seq: Seq[Int], k: Int): Double = {
    (0 until seq.length - k)
      .map(i => seq(i) * seq(i + k))
      .sum
  }

  def energy(seq: Seq[Int]): Double = {
    (1 until seq.length)
      .map { i =>
        val corr = autocorr(seq, i)
        corr * corr
      }
      .sum
  }

  def allSeqs(elements: Seq[Int], len: Int): Seq[Seq[Int]] = {
    (1 to len)
      .map(_ => elements)
      .foldLeft(Seq(Seq.empty[Int])) {
        (x, y) => for (a <- x.view; b <- y) yield a :+ b
      }
  }

  def randomSeq(seqs: Seq[Seq[Int]]): Seq[Int] = Random.shuffle(seqs).head

  def neighbours(seq: Seq[Int]): Seq[Seq[Int]] = (0 until seq.length).map(i => seq.updated(i, seq(i) * (-1)))

  def run(path: List[Seq[Int]], iter: Int): List[Seq[Int]] = {
    //if (iter > 1000 || energy(path.last) < 10)
    if (energy(path.last) < 10)
      path
    else {
      val ngbh = neighbours(path.last).filter(n => !path.contains(n)).map(n => (energy(n), n))
      if (ngbh.isEmpty) {
        path
      } else {
        val (_, bestNeighbour) = ngbh.maxBy(_._1) //FIXME ngbh might be empty
        run(path :+ bestNeighbour, iter + 1)
      }

    }
  }


  def selfAvoidingWalk(n: Int) = {
    val intialSequence = randomSeq(allSeqs(Seq(1, -1), n))
    val path = List(intialSequence)
    run(path, 1000).map(seq => (energy(seq), seq)).maxBy(_._1)
  }

  //allSeqs(Seq(-1, 1), 10)
  //	.map( seq => (energy(seq.toList), seq))
  //	.minBy(_._1)

  println(energy(Seq(1, -1, 1, -1, 1, -1, 1, -1))) //140
  println(energy(Seq(1, -1, 1, -1, 1, -1, 1, 1))) //56
  println(energy(Seq(1, -1, 1, -1, 1, -1, -1, 1))) //36
  println(energy(Seq(1, 1, 1, -1, 1, -1, -1, 1))) //8

  println(selfAvoidingWalk(10))

}

