package proservers.cores

import org.opencv.core.{ Range => _, _ }
import org.opencv.features2d._
import org.opencv.highgui.Highgui
import proservers.utils.{ ImageDetectDriver, ImageUtils }

import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-07-05.
 *
 */

trait ImageDetectCVServer extends ImageDetectDriver with ImageUtils {

  def imageContrast(pathA: String, pathB: String): Boolean = {
    try {
      val imageA: Mat = Highgui.imread(pathA, Highgui.CV_LOAD_IMAGE_COLOR)
      val imageB: Mat = Highgui.imread(pathB, Highgui.CV_LOAD_IMAGE_COLOR)
      val keypointsA: MatOfKeyPoint = new MatOfKeyPoint()
      val keypointsB: MatOfKeyPoint = new MatOfKeyPoint()
      val descriptorsA: Mat = new Mat()
      val descriptorsB: Mat = new Mat()
      val detector: FeatureDetector = FeatureDetector.create(FeatureDetector.ORB)
      val extractor: DescriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB)

      detector.detect(imageA, keypointsA)
      detector.detect(imageB, keypointsB)
      imageA.release(); imageB.release(); keypointsA.release(); keypointsB.release()

      extractor.compute(imageA, keypointsA, descriptorsA)
      extractor.compute(imageB, keypointsB, descriptorsB)
      imageA.release(); imageB.release(); keypointsA.release(); keypointsB.release(); descriptorsA.release(); descriptorsB.release()

      val matcher: DescriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING)
      val matches: MatOfDMatch = new MatOfDMatch()

      if (descriptorsB.cols() == descriptorsA.cols()) matcher.`match`(descriptorsA, descriptorsB, matches)
      matcher.clear()

      matches.release()
      var matchArray: Array[DMatch] = matches.toArray
      matches.release()

      def release() = {
        imageA.release(); imageB.release(); keypointsA.release(); keypointsB.release()
        descriptorsA.release(); descriptorsB.release(); matches.release()
        println(imageA.empty(), imageB.empty(), keypointsA.empty(), keypointsB.empty(), descriptorsA.empty(), descriptorsB.empty(), matches.empty())
      }

      if (matchArray.isEmpty) {
        matchArray = null
        release(); false
      } else {
        descriptorsA.release()
        val rets = (0 until descriptorsA.rows()).map { i => matchArray(i).distance }
        release()
        matchArray = null
        rets.exists(_ <= 25)
      }

    } catch {
      case NonFatal(e) =>
        println(s"ImageDetectServer.imageContrast: ${e.getMessage}")
        e.printStackTrace(); false
    }
  }

  def imageDetect(path: String, format: String): Option[String] = {
    try {
      val faceDetections: MatOfRect = new MatOfRect()
      val image: Mat = Highgui.imread(path)
      val (originW: Int, originH: Int) = (image.width(), image.height())

      (graynessDetections(image), generateTargetSize(originW, originH)) match {
        case (true, Some((targetW: Int, targetH: Int))) =>
          val (targetCenterX: Int, targetCenterY: Int) = (targetW / 2, targetH / 2)
          faceDetector.detectMultiScale(image, faceDetections)
          println(s"Detected ${faceDetections.toArray.length} faces")
          faceDetections.toArray.foreach {
            println(_)
          }

          val targetRect: Rect = mergeDetections(filterDetections(faceDetections.toArray, originW, originH)) match {
            case None =>
              new Rect(0, 0, targetW, targetH)
            // new Rect((originW - targetW) / 2, (originH - targetH) / 2, targetW, targetH)
            case Some((detectCenterX, detectCenterY)) =>
              val moveX = detectCenterX - targetCenterX
              val moveY = detectCenterY - targetCenterY

              val (left: Int, right: Int) = if (moveX <= 0) {
                (0, targetW)
              } else if (moveX > 0 && moveX < originW - targetW) {
                (moveX, moveX + targetW)
              } else (originW - targetW, originW)

              val (top: Int, botom: Int) = if (moveY <= 0) {
                (0, targetH)
              } else if (moveY > 0 && moveY < originH - targetH) {
                (moveY, moveY + targetH)
              } else (originH - targetH, originH)
              new Rect(left, top, right - left, botom - top)
          }

          val newPath: String = generatePath(path, format = s"_${targetW}X$targetH.$format")
          val imageRoi: Mat = new Mat(image, targetRect)
          Highgui.imwrite(newPath, imageRoi)

          faceDetections.release(); image.release(); imageRoi.release()

          Some(newPath)
        case _ => None
      }
    } catch {
      case NonFatal(e) =>
        println(s"ImageDetectServer.imageDetect: ${e.getMessage}")
        e.printStackTrace(); None
    }
  }

  private def generateTargetSize(originW: Int, originH: Int): Option[(Int, Int)] = {
    val targetSize = (for {
      width <- Range(originW, 1, -1)
      height <- Range(originH, 1, -1)
      if (width.toDouble / height) == 4.0 / 3
    } yield (width, height)).headOption
    if (targetSize.nonEmpty && targetSize.get._1 > 160 && (originW / originH) < 4)
      targetSize
    else None
  }

  private def filterDetections(detections: Array[Rect], originW: Int, originH: Int): Array[Rect] = {
    detections.collect {
      case rect if rect.x + rect.width / 2 > originW * 0.2 &&
        rect.x + rect.width / 2 < originW * 0.8 &&
        rect.y + rect.height / 2 < originH * 0.7 => rect
    }
  }

  private def mergeDetections(detections: Array[Rect]): Option[(Int, Int)] = {
    detections.toList match {
      case Nil         => None
      case rect :: Nil => Some((rect.x + rect.width / 2, rect.y + rect.height / 2))
      case head :: tail =>
        val headPair = (head.x + head.width / 2, head.y + head.height / 2)
        val tailPairs = tail.map { r => (r.x + r.width / 2, r.y + r.height / 2) }
        val finalX = tailPairs.map(_._1).fold(headPair._1)((ax, bx) => ax + (bx - ax) / 2)
        val finalY = tailPairs.map(_._2).fold(headPair._2)((ax, bx) => ax + (bx - ax) / 2)
        Some((finalX, finalY))
    }
  }

  private final val GRAY_LIMIT = 50
  private final val WHITE_LIMIT = 200
  private final val RATIO_LIMIT = 0.85

  private def graynessDetections(image: Mat): Boolean = {
    val (originW: Int, originH: Int) = (image.width(), image.height())

    var grayCount: Double = 0
    var whitCount: Double = 0

    for {
      x <- Range(0, originW)
      y <- Range(0, originH)
    } {
      val pixelRGB: Seq[Double] = image.get(y, x).collect { case v: Double => v }
      if (pixelRGB.sum / pixelRGB.length < GRAY_LIMIT) grayCount += 1
      else if (pixelRGB.sum / pixelRGB.length > WHITE_LIMIT) whitCount += 1
    }
    grayCount / (originW * originH) < RATIO_LIMIT && whitCount / (originW * originH) < RATIO_LIMIT
  }
}

object ImageDetectCVServer extends ImageDetectCVServer