package proservers.utils

import com.aliyun.oss.OSSClient

/**
 * Created by zhange on 2016-06-23.
 *
 */

trait OssDriver {
  val endpoint = "oss-cn-hangzhou.aliyuncs.com"
  val accessKeyId = "QK8FahuiSCpzlWG8"
  val accessKeySecret = "TGXhTCwUoEU4yNEGsfZSDvp0dNqw2p"
  val bucketName = "bdp-images"
  val client: OSSClient = new OSSClient(endpoint, accessKeyId, accessKeySecret)
}

object OssDriver extends OssDriver {
  def ossClient: OSSClient = client
}