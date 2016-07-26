package proservers.utils

import com.aliyun.oss.OSSClient

/**
 * Created by zhange on 2016-06-23.
 *
 */

trait OssDriver extends Config {
  val client: OSSClient = new OSSClient(endpoint, accessKeyId, accessKeySecret)
}

object OssDriver extends OssDriver {
  def ossClient: OSSClient = client
}