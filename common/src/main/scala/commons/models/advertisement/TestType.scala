package commons.models.advertisement

import play.api.libs.json.Json

/**
 * Created by zhangshl on 16/8/31.
 */
object TestType {
  def main(args: Array[String]) {
    val json: String = "{\"version\":1.0,\"status\":0,\"message\":\"Success\",\"data\":{\"adspace\":[{\"aid\":100,\"creative\":[{\"cid\":126,\"index\":0,\"app\":{\"app_name\":\"横扫千军\",\"app_package\":\"com.xd.hsqj\",\"app_size\":0},\"impression\":[\"http://as.lieying.cn/imp.html?version=1.0&aid=100&sid=26&cid=126&mid=0&guid=c40da3770d3540a79cc2a2616480ea2f&lon=%%LON%%&lat=%%LAT%%\"],\"event\":[{\"event_key\":2,\"event_value\":\"http://as.lieying.cn/click.html?version=1.0&aid=100&sid=26&cid=126&mid=0&guid=c40da3770d3540a79cc2a2616480ea2f&lon=%%LON%%&lat=%%LAT%%&downx=%%DOWNX%%&downy=%%DOWNY%%&upx=%%UPX%%&upy=%%UPY%%&target=http%3A%2F%2Falicdn.lieying.cn%2Fsentshow%2Fimgs%2Ftmp%2FxindongG_0617.apk\"}],\"ad_native\":[{\"index\":0,\"type\":\"jpg\",\"template_id\":21,\"index_value\":\"image1\",\"required_field\":2,\"action_type\":0,\"required_value\":\"http://alicdn.lieying.cn/sentshow/imgs/20160824/0eb4bd2b-3405-4eab-8b63-ea9a6b62c4f8.jpg\"},{\"index\":1,\"type\":\"jpg\",\"template_id\":21,\"index_value\":\"image2\",\"required_field\":2,\"action_type\":0,\"required_value\":\"http://alicdn.lieying.cn/sentshow/imgs/20160824/94d175aa-c58f-495a-b2d4-2436c86c55f5.jpg\"},{\"index\":2,\"type\":\"jpg\",\"template_id\":21,\"index_value\":\"image3\",\"required_field\":2,\"action_type\":0,\"required_value\":\"http://alicdn.lieying.cn/sentshow/imgs/20160824/2e337583-ea99-4ac6-8764-a410554dccd5.jpg\"},{\"index\":3,\"type\":\"text\",\"template_id\":21,\"index_value\":\"title\",\"required_field\":1,\"action_type\":0,\"required_value\":\"《横扫千军》心动倾情研发的策略国战手游\"}]}],\"adformat\":5}]}}"
    val t: AdResponse = Json.parse(json).as[AdResponse]
    println(t)
  }

}
