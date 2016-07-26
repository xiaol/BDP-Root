package proservers.cores

import scala.util.control.NonFatal
import java.io.File

import org.openimaj.feature.local.SpatialLocation
import org.openimaj.feature.local.list.LocalFeatureList
import org.openimaj.feature.local.matcher._
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d
import org.openimaj.image._
import org.openimaj.image.colour.RGBColour
import org.openimaj.image.feature.local.engine.DoGSIFTEngine
import org.openimaj.image.feature.local.keypoints.Keypoint
import org.openimaj.image.processing.face.detection.HaarCascadeDetector
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator
import org.openimaj.math.model.fit.RANSAC
import org.openimaj.image.processing.resize.{ BilinearInterpolation, ResizeProcessor }
import proservers.utils.ImageUtils

/**
 * Created by zhange on 2016-07-20.
 *
 */

object ImageDetectIMAJServer extends ImageDetectIMAJServer

trait ImageDetectIMAJServer extends ImageUtils {

  private val compareEngine: DoGSIFTEngine = new DoGSIFTEngine()

  def imageContrast(pathA: String, pathB: String): Boolean = {
    try {
      val imageA: MBFImage = ImageUtilities.readMBF(new File(pathA))
      val imageB: MBFImage = ImageUtilities.readMBF(new File(pathB))

      val keypointsA: LocalFeatureList[Keypoint] = compareEngine.findFeatures(imageA.flatten())
      val keypointsB: LocalFeatureList[Keypoint] = compareEngine.findFeatures(imageB.flatten())

      val modelFitter: RobustAffineTransformEstimator = new RobustAffineTransformEstimator(5.0, 1500,
        new RANSAC.PercentageInliersStoppingCondition(0.5))
      val matcher = new ConsistentLocalFeatureMatcher2d[Keypoint](
        new FastBasicKeypointMatcher[Keypoint](8), modelFitter)

      matcher.setModelFeatures(keypointsA)
      matcher.findMatches(keypointsB)

      matcher.getMatches.size() > 5
    } catch {
      case NonFatal(e) =>
        println(s"ImageDetectIMAJServer.imageContrast: ${e.getMessage}")
        false
    }
  }

  def imageDetect(path: String, format: String) = {
    try {
      val image: FImage = ImageUtilities.readF(new File(path))
      val imageRGB: MBFImage = ImageUtilities.readMBF(new File(path))
      val (originW: Int, originH: Int) = (image.getWidth, image.getHeight)

      (graynessDetections(imageRGB), generateTargetSize(originW, originH)) match {
        case (true, Some((targetW: Int, targetH: Int))) =>
          val faces: List[(Int, Int)] = faceDetector(image.clone())
          val finalFace = mergeDetections(filterDetections(faces, originW, originH)) match {
            case None =>
              val left = (originW - targetW) / 2
              val right = left + targetW
              val top = 0
              val botom = targetH
              (left, top)
            case Some((detectCenterX, detectCenterY)) =>
              val moveX = detectCenterX - targetW / 2
              val moveY = detectCenterY - targetH / 2

              val (left: Int, right: Int) = if (moveX <= 0) {
                (0, targetW)
              } else if (moveX > 0 && moveX + targetW < originW) {
                (moveX, moveX + targetW)
              } else (originW - targetW, originW)

              val (top: Int, botom: Int) = if (moveY <= 0) {
                (0, targetH)
              } else if (moveY > 0 && moveY + targetH < originH) {
                (moveY, moveY + targetH)
              } else (originH - targetH, originH)
              (left, top)
          }
          val newPath: String = generatePath(path, format = s"_${targetW}X$targetH.$format")

          val canvas: MBFImage = new MBFImage(targetW, targetH)
          val cropPosition: SpatialLocation = new SpatialLocation()
          cropPosition.x = -finalFace._1
          cropPosition.y = -finalFace._2

          canvas.drawImage(imageRGB, cropPosition)
          imageRGB.internalAssign(canvas)
          ImageUtilities.write(imageRGB, format, new File(newPath))
          Some(newPath)
        case _ => None
      }
    } catch {
      case NonFatal(e) =>
        println(s"ImageDetectIMAJServer.imageDetect: ${e.getMessage}")
        None
    }
  }

  private val faceDetectDriver = new HaarCascadeDetector()

  def faceDetector(image: FImage): List[(Int, Int)] = {
    val faces = faceDetectDriver.detectFaces(image)

    (for {
      i <- 0 until faces.size()
    } yield {
      val shape = faces.get(i).getShape
      (shape.minX() + (shape.maxX() - shape.minX()) / 2, shape.minY() + (shape.maxY() - shape.minY()) / 2)
    }).toList.map { case (x, y) => (x.toInt, y.toInt) }
  }

  private def generateTargetSize(originW: Int, originH: Int): Option[(Int, Int)] = {
    val targetSize = (for {
      width <- Range(originW, 1, -1)
      height <- Range(originH, 1, -1)
      if (width.toDouble / height) == 4.0 / 3
    } yield (width, height)).headOption
    if (targetSize.isDefined && targetSize.get._1 > 160 && (originW / originH) < 4) {
      targetSize
    } else None
  }

  private def filterDetections(detections: List[(Int, Int)], originW: Int, originH: Int): List[(Int, Int)] = {
    detections.collect {
      case (x, y) if x > originW * 0.2 &&
        x < originW * 0.8 &&
        y < originH * 0.6 => (x, y)
    }
  }

  private def mergeDetections(detections: List[(Int, Int)]): Option[(Int, Int)] = {
    detections match {
      case Nil           => None
      case center :: Nil => Some(center)
      case head :: tail =>
        val finalX = tail.map(_._1).fold(head._1)((ax, bx) => ax + (bx - ax) / 2)
        val finalY = tail.map(_._2).fold(head._2)((ax, bx) => ax + (bx - ax) / 2)
        Some((finalX, finalY))
    }
  }

  private final val GRAY_LIMIT = 0.3F
  private final val WHITE_LIMIT = 0.7F
  private final val RATIO_LIMIT = 0.75D

  private def graynessDetections(image: MBFImage): Boolean = {
    val (originW: Int, originH: Int) = (image.getWidth, image.getHeight)
    var grayCount: Double = 0
    var whitCount: Double = 0

    for (x <- 0 until image.getWidth; y <- 0 until image.getHeight) {
      val r = image.getBand(0).pixels(y)(x)
      val g = image.getBand(1).pixels(y)(x)
      val b = image.getBand(2).pixels(y)(x)

      if ((r + g + b) / 3 < GRAY_LIMIT) grayCount += 1
      else if ((r + g + b) / 3 > WHITE_LIMIT) whitCount += 1
    }

    grayCount / (originW * originH) < RATIO_LIMIT && whitCount / (originW * originH) < RATIO_LIMIT
  }
}

