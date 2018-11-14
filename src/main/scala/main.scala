
object HelloWorld {

  def autocorr(seq: Seq[Int], k: Int): Double = {
          (0 until seq.length - k)
        .map((i) => seq(i) * seq(i+k))
        .sum
  }

  def energy(seq: Seq[Int]): Double = {
    (1 until seq.length)
        .map {i => 
          val corr = autocorr(seq, i) 
          corr * corr
        }
        .sum
  }

  def allSeqs(elements: Seq[Int], len: Int): Seq[Seq[Int]] = {
    (1 to len)
      .map( _ => elements)
      .foldLeft(Seq(Seq.empty[Int])) {
          (x, y) => for (a <- x.view; b <- y) yield a :+ b
      }
  }

  def main(args: Array[String]): Unit = {
    //allSeqs(Seq(-1, 1), 10)
    //	.map( seq => (energy(seq.toList), seq))
    //	.minBy(_._1)

    println(energy(Seq(1, -1, 1, -1, 1, -1, 1, -1))) //140
    println(energy(Seq(1, -1, 1, -1, 1, -1, 1, 1))) //56
    println(energy(Seq(1, -1, 1, -1, 1, -1, -1, 1))) //36
    println(energy(Seq(1, 1, 1, -1, 1, -1, -1, 1))) //8
  }
}

