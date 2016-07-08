package proservers.utils

import org.opencv.core.Core
import org.opencv.objdetect.CascadeClassifier

/**
 * Created by zhange on 2016-07-05.
 *
 */

trait ImageDetectDriver {
  System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
  val faceDetector: CascadeClassifier = new CascadeClassifier(getClass.getResource("/lbpcascade_frontalface.xml").getPath)
  println("Initializing FaceDetector...")
}
