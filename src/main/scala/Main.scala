import scala.util.Random
import com.typesafe.config.ConfigFactory
import com.concurrentthought.cla.Opt
import com.concurrentthought.cla.Args
import grizzled.slf4j.Logger
import org.slf4j.MDC

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
    if (iter > config.getInt("iterationsNumber") || skewEnergy(path.last) < config.getInt("energyThreshold"))
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
    run(path, 0)
      .map(generateSkewSymmetry)
      .map(seq => (energy(seq), seq))
      .minBy(_._1)
  }

  def workFor(n: Int, start: Double, bestPaths: List[SeqEnergy], lastLog: Double): SeqEnergy = {
    start.toDouble + n.toDouble*1000 compare System.currentTimeMillis() match {
      case 0   => bestPaths.minBy(_._1)
      case -1  => bestPaths.minBy(_._1)
      case 1 => {
        //FIXME - it prints multiple values at once
        val now = System.currentTimeMillis()
        if (now - lastLog > config.getDouble("printEvery") * 1000) {
          val (bestEnergy, bestSequence) = if (bestPaths.nonEmpty) bestPaths.minBy(_._1) else (config.getDouble("bigNumber"), Seq())
          val meritFactor = scala.math.pow(bestSequence.length.toDouble, 2.0) / (2.0 * bestEnergy.asInstanceOf[Double])
          val now = System.currentTimeMillis()
          logger.info(s"Merit Factor: $meritFactor, Energy: $bestEnergy, Time: $now")
          workFor(n, start, bestPaths :+ selfAvoidingWalk(config.getInt("seriesLength")), now)
        } else workFor(n, start, bestPaths :+ selfAvoidingWalk(config.getInt("seriesLength")), lastLog)
      }
    }
  }

  def workFor(n: Int): (Double, Seq[Int]) = workFor(n, System.currentTimeMillis(), List(), 0.0)
 /*
  println(energy(Seq(1, -1, 1, -1, 1, -1, 1, -1))) //140
  println(energy(Seq(1, -1, 1, -1, 1, -1, 1, 1))) //56
  println(energy(Seq(1, -1, 1, -1, 1, -1, -1, 1))) //36
  println(energy(Seq(1, 1, 1, -1, 1, -1, -1, 1))) //8
  */

  val conf  = Opt.string(
      name     = "conf",
      flags    = Seq("-c", "--config"),
      help     = "Path to config file.",
      requiredFlag = true)
  
  val output = Opt.string(
      name     = "output",
      flags    = Seq("-o", "--out"),
      default  = Some("/dev/null"),
      help     = "Path to output file.")

  val finalArgs: Args = Args(Seq(conf, output)).process(args)
  print(finalArgs.getOrElse("conf", false))
  val config = ConfigFactory.load(finalArgs.getOrElse("conf", "default.conf")).getConfig("ea")
  System.setProperty("log.name", finalArgs.getOrElse("output", "ea.log"))
  val logger = Logger("ea")

  workFor(config.getInt("time"))
  logger.info("------------")
}
