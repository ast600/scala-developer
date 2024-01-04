package module1.datacollections.homework

import scala.util.Random
import scala.collection.immutable

class BallsExperiment {
  private val ballsUrn = immutable.List(0, 0, 0, 1, 1, 1)

  def isFirstBlackSecondWhite(): Boolean = {
    val shuffledUrn = Random.shuffle(ballsUrn)
    val firstTwo = shuffledUrn.take(2)

    firstTwo.head == 0 && firstTwo.last == 1
  }

  def isFirstBlack(): Boolean = {
    val shuffledUrn = Random.shuffle(ballsUrn)

    shuffledUrn.head == 0
  }
}

object BallsTest {
  def main(args: Array[String]): Unit = {
    val count = 10000
    val listOfExperiments: List[BallsExperiment] = (1 to count).map { _: Int => new BallsExperiment }.toList
    val countOfExperiments = listOfExperiments.map { _.isFirstBlackSecondWhite() }
    val countOfPositiveExperiments: Float = countOfExperiments.count(_ == true)
    // Вероятность события "первый - черный, второй - белый" без условия
    println(countOfPositiveExperiments / count)
    // Условная вероятность
    val countOfPositiveExperimentsFirstBlackOnly = listOfExperiments.map { _.isFirstBlack() }
                                                                    .count { _ == true }
    println(countOfPositiveExperiments / countOfPositiveExperimentsFirstBlackOnly)
  }
}