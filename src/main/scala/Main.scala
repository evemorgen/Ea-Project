import com.concurrentthought.cla.{Args, Opt}
import com.typesafe.config.{ConfigFactory, Config}
import grizzled.slf4j.Logger

import scala.annotation.tailrec
import scala.util.Random

object Main extends App {

  type SeqEnergy = (Double, Seq[Int])

  def autocorr(seq: Seq[Int], k: Int): Double = {
    (0 until seq.length - k)
      .map(i => seq(i) * seq(i + k))
      .sum
  }

  def skewEnergy(seq: Seq[Int]): Double = {
    energy(generateSkewSymmetry(seq), 2, 2)
  }

  def energy(seq: Seq[Int], step: Int = 1, start: Int = 1): Double = {
    (start until seq.length by step)
      .map { i =>
        val corr = autocorr(seq, i)
        corr * corr
      }
      .sum
  }

  def generateSkewSymmetry(elements: Seq[Int]): Seq[Int] = {
    elements ++ elements.dropRight(1).reverse.zipWithIndex.map {
      case (element, index) if (index+1) % 2 == 1 => -element
      case (element, _) => element
    }
  }

  def randomSeq(n: Int): Seq[Int] = (1 to n).map(_ => Random.shuffle(Seq(1, -1)).head)

  def neighbours(seq: Seq[Int]): Seq[Seq[Int]] = seq.indices.map(i => seq.updated(i, seq(i) * -1))

  @tailrec
  def run(path: List[Seq[Int]], iter: Int, config: Config): List[Seq[Int]] = {
    if (iter > config.getInt("iterationsNumber") ||
      skewEnergy(path.last) < config.getInt("energyThreshold")
    )
      path
    else {
      val ngbh = neighbours(path.last)
        .filter(n => !path.contains(n))
        .map(n => (skewEnergy(n), n))
      if (ngbh.isEmpty) {
        path
      } else {
        val (bestValue, bestNeighbour) = ngbh.minBy(_._1)
        run(path :+ bestNeighbour, iter + 1, config)
      }
    }
  }

  def selfAvoidingWalk(config: Config): SeqEnergy = {
    val n = config.getInt("seriesLength")
    val intialSequence = randomSeq((n / 2).toInt + 1)
    val path = List(intialSequence)
    run(path, 0, config)
      .map(generateSkewSymmetry)
      .map(seq => (energy(seq), seq))
      .minBy(_._1)
  }

  def workFor(start: Double, bestPaths: List[SeqEnergy], lastLog: Double, config: Config): SeqEnergy = {
    val n = config.getInt("time")
    start.toDouble + n.toDouble * 1000 compare System.currentTimeMillis() match {
      case 0 => bestPaths.minBy(_._1)
      case -1 => bestPaths.minBy(_._1)
      case 1 => {
        //FIXME - it prints multiple values at once
        val now = System.currentTimeMillis()
        if (now - lastLog > config.getDouble("printEvery") * 1000) {
          val (bestEnergy, bestSequence) = if (bestPaths.nonEmpty) bestPaths.minBy(_._1) else (config.getDouble("bigNumber"), Seq())
          val meritFactor = scala.math.pow(bestSequence.length.toDouble, 2.0) / (2.0 * bestEnergy.asInstanceOf[Double])
          val now = System.currentTimeMillis()
          logger.info(s"Merit Factor: $meritFactor, Energy: $bestEnergy, Time: $now, seq: $bestSequence")
          workFor(start, bestPaths ++ parSelfAvoidingWalk(config), now, config)
        } else workFor(start, bestPaths ++ parSelfAvoidingWalk(config), lastLog, config)
      }
    }
  }

  private def parSelfAvoidingWalk(config: Config)
    = (0 until Runtime.getRuntime.availableProcessors).par.map(_ => selfAvoidingWalk(config))
//    = Seq(selfAvoidingWalk(config))

  def workFor(config: Config): (Double, Seq[Int]) = workFor(System.currentTimeMillis(), List(), 0.0, config)

  val conf = Opt.string(
    name = "conf",
    flags = Seq("-c", "--config"),
    help = "Path to config file.",
    requiredFlag = true)

  val output = Opt.string(
    name = "output",
    flags = Seq("-o", "--out"),
    default = Some("/dev/null"),
    help = "Path to output file.")

  val finalArgs: Args = Args(Seq(conf, output)).process(args)
  val config = ConfigFactory.load(finalArgs.getOrElse("conf", "default.conf")).getConfig("ea")
  System.setProperty("log.name", finalArgs.getOrElse("output", "ea.log"))
  val logger = Logger("ea")
  workFor(config)
  //  val seq = Seq(1, 1, 1, 1, 1, 1, 1, -1, -1, 1, -1, -1, 1, -1, 1, 1, -1, -1, -1, 1, 1, -1, 1, -1, 1, -1, 1)
  //  val seq = Seq(-1,-1,-1,1,1,1,1,-1,-1,-1,1,-1,-1,-1,1,-1,-1,-1,1,-1,-1,1,-1,1,1,-1,1)
  //  println(seq)
  //  println(generateSkewSymmetry(seq.slice(0, 14)))
  //  println(energy(seq))
  //  println(skewEnergy(seq.slice(0, 14)))
  logger.info("------------")
}
