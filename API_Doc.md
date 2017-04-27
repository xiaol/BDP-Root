# 数据平台接口文档_V3.14

# 目录
[TOC]

----
## 更新日志
*V3.14:*
1. 列表页刷新（广告）/v2/ns/fed/ra、列表页加载（广告）/v2/ns/fed/la：添加二级频道查询功能

*V3.13:*
1. 版本更新接口：/v2/version/query

*V3.12:*
1. 爬虫上传热点新闻接口:/v2/hot/crawler/news
2. 爬虫上传热词接口:/v2/hot/crawler/words
3. 前端获取热词接口:/v2/hot/words

*V3.11:*

1. 获取广告来源接口:/v2/ad/source
2. feed流、相关新闻、普通新闻评论的请求接口中需要增加ads参数; 猎鹰广告api:1 ,广点通sdk:2 ,亦复广告api:3 ;

*V3.10:*

1. 相关新闻添加广告:/v2/ns/ascad
2. 修改滑动统计接口：/v2/sl/ins

*V3.9:*

1. 第三方用户注册添加utype：
黄历天气：12，
纹字锁频：13，
猎鹰浏览器：14，
白牌：15

*V3.8:*

1. feed流接口（/v2/ns/fed/ra、/v2/ns/fed/la、/v2/ns/fed/rn、/v2/ns/fed/ln）、专题详情接口(/v2/ns/tdq) 、新闻相关列表接口(/v2/ns/c)：添加两个字段logtype、logchid
2. 点击日志上报接口(/rep/v2/c)：多上传两个字段(请注意上传日志中的数据类型，否则上传成功了，后台解析不了)

*V3.7:*

1. 添加接口: 滑动统计接口
2. 添加接口: 详情页广告


----
## 通用注释

### 错误码

1. HTTP标准错误码；
2. 2000：服务端成功；
3. 2001：服务端错误；
4. 2002：服务端未找到数据；
5. 2003：服务端数据创建失败；
6. 2004：服务端数据删除失败；
7. 4001：请求数据错误；
8. 4002：请求体JSON格式错误；
9. 4003：用户验证错误；
10. 4015: 请求Content-Type格式不正确；

### 图片服务

图片原图 HOST(接口中返回的图片 HOST)：bdp-pic.deeporiginalx.com
图片处理 HOST(用于图片处理的 HOST)：pro-pic.deeporiginalx.com

注释：原图 URL 中已经增加了图片的尺寸，使用符号大写的字母`X`分割，前面为 **宽**，后面为 **高**，即 **...ExOWM_宽X高.jpg**。

如果需要获取对应尺寸的图片，参考下面的流程：

1. 首先将获得的图片 URL 中的 HOST(即原图 HOST)替换为`图片处理的 HOST`:
   `http://bdp-pic.deeporiginalx.com/W0JAN2RjNjExOWM_397X220.jpg`
   => `http://pro-pic.deeporiginalx.com/W0JAN2RjNjExOWM_397X220.jpg`
2. 参考《阿里云 OSS 图片处理服务》的官方文档，设置处理脚本，比如：
   `1e_1c_0o_0l_100sh_200h_300w_95q.src` 表示：按短边缩放、居中裁剪、缩减宽高分别为(300,200)、锐化100、质量95
3. 将编辑好的脚本使用符号`@`与第一步中的 URL 连接得到最终的缩略图 URL：
   `http://pro-pic.deeporiginalx.com/W0JAN2RjNjExOWM_397X220.jpg@1e_1c_0o_0l_100sh_200h_300w_95q.src`

如果只有宽高缩放的需求，可以直接将上面脚本中的`200h_300w`替换为需要的尺寸，如果有更多需求，参考 OSS 服务配置文档。

----
##  客户端接口

### 1 用户

用户平台注释（platform）：

| 平台   | 代码   |
| ---- | :--- |
| IOS  | 1    |
| 安卓   | 2    |
| 网页   | 3    |
| 无法识别 | 4    |

用户类型注释（utype）：

| 类型     | 代码   |
| ------ | :--- |
| 本地注册用户 | 1    |
| 游客用户   | 2    |
| 微博三方用户 | 3    |
| 微信三方用户 | 4    |
| 黄历天气 | 12    |
| 纹字锁频 | 13    |
| 猎鹰浏览器 | 14    |
| 白牌 | 15    |

----
#### 1.1 游客用户

##### 1.1.1 游客用户注册

_Request_

```json
POST /v2/au/sin/g
Host: bdp.deeporiginalx.com
Content-Type: application/json

{
  "utype": 2,
  "platform": 1,
  "province": "北京市",   - Option
  "city": "北京市",       - Option
  "district": "东城区"    - Option
}
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic OWNtcWFibHYoN2tzZ2MuZyoqN18uZjQydS50bnBpNSlnYmR+filwdW4qaWVpM2xzNXhxOH4qeWdzMWRuOXVndQ

{
  "code": 2000,
  "data": {
    "utype": 2,
    "uid": 22,
    "password": "YWU1YzhmZGYtNDY3My00NTZjLTk3OGYtZDM0MTljMmVlYWVj"
  }
}
```

----
##### 1.1.2 游客用户登录

_Request_

```json
POST /v2/au/lin/g
Host: bdp.deeporiginalx.com
Content-Type: application/json

{
  "uid": 22,
  "password": "YWU1YzhmZGYtNDY3My00NTZjLTk3OGYtZDM0MTljMmVlYWVj"
}
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X3VmNDM5YXQ3MWdlJ28qNGMyJzlyfnJpZG9+dicybDZrNydhdXcyaW82MmFsc20pdV8xcDh6aTY5bmwuNl9vKA

{
  "code": 2000,
  "data": {
    "utype": 2,
    "uid": 22,
    "password": "YWU1YzhmZGYtNDY3My00NTZjLTk3OGYtZDM0MTljMmVlYWVj"
  }
}
```

----
#### 1.2 三方用户注册

_Request_

```json
POST /v2/au/sin/s
Host: bdp.deeporiginalx.com
Content-Type: application/json

{
  "muid": 4,                          - Option - 游客合并三方时提供该字段
  "msuid": "weibo_suid_xxx",          - Option - 三方之间进行合并时提供该字段
  "utype": 3,                         - 用户类型 - 本地注册用户:1, 游客用户:2 ,微博三方用户:3 ,微信三方用户:4, 黄历天气:12, 纹字锁频:13, 猎鹰浏览器:14, 白牌:15
  "platform": 1,                      - 平台类型 - IOS:1, 安卓:2, 网页:3, 无法识别:4
  "suid": "weibo_suid_xxx1",          - 第三方用户id 
  "stoken": "weibo_stoken_xxx",       - 第三方登录stoken
  "sexpires": "2016-4-27 17:37:22",   - 过期时间
  "uname": "zhange",                  - 用户名
  "gender": 1,                        - Option - 性别
  "avatar": "http://tva4.jpg",        - Option - 头像地址
  "averse": [                         - Option - 用户屏蔽字段列表
    "政治",
    "战争",
    "腐败"
  ],
  "prefer": [                         - Option - 用户偏好字段列表
    "体育",
    "音乐",
    "杂志"
  ],
  "province": "河南省",               - Option
  "city": "郑州市",                   - Option
  "district": "二七区"                - Option
}
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA

{
  "code": 2000,
  "data": {
    "utype": 3,
    "uid": 5,
    "uname": "zhange",
    "avatar": "http://tva4.sinaimg.cn/crop.0.0.1080.1080.180/e56a51c9jw8erh17sxq87j20u00u0jta.jpg"
  }
}
```
----
#### 1.3 本地用户

##### 1.3.1 本地用户注册

_Request_

```json
POST /v2/au/sin/l
Host: bdp.deeporiginalx.com
Content-Type: application/json

{
  "utype": 1,
  "platform": 1,
  "uid": 22,    - Option
  "uname":"lieying",
  "email":"lieying@lieying.cn",
  "password":"******",
  "province": "北京市",   - Option
  "city": "北京市",       - Option
  "district": "东城区"    - Option
}
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic OWNtcWFibHYoN2tzZ2MuZyoqN18uZjQydS50bnBpNSlnYmR+filwdW4qaWVpM2xzNXhxOH4qeWdzMWRuOXVndQ

{
  "code": 2000,
  "data": {
    "utype": 1,
    "uid": 22,
    "uname": "lieying"
  }
}
```
----
##### 1.3.2 本地用户登录

_Request_

```json
POST /v2/au/lin/g
Host: bdp.deeporiginalx.com
Content-Type: application/json

{
  "email": "lieying@lieying.cn",
  "password": "******"
}
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X3VmNDM5YXQ3MWdlJ28qNGMyJzlyfnJpZG9+dicybDZrNydhdXcyaW82MmFsc20pdV8xcDh6aTY5bmwuNl9vKA

{
  "code": 2000,
  "data": {
    "utype": 1,
    "uid": 22,
    "uname": "lieying"
  }
}
```
----
##### 1.3.3 本地用户修改密码

_Request_

```json
POST /v2/au/lin/c
Host: bdp.deeporiginalx.com
Content-Type: application/json

{
  "email": "lieying@lieying.cn",
  "oldpassword": "******",
  "newpassword": "******",
  "verification": ""    - Option
}
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X3VmNDM5YXQ3MWdlJ28qNGMyJzlyfnJpZG9+dicybDZrNydhdXcyaW82MmFsc20pdV8xcDh6aTY5bmwuNl9vKA

{
  "code": 2000,
  "data": {
    "utype": 1,
    "uid": 22,
    "uname": "lieying"
  }
}
```
----
##### 1.3.4 本地用户重置密码

_Request_

```json
POST /v2/au/lin/r
Host: bdp.deeporiginalx.com
Content-Type: application/json

{
  "email": "lieying@lieying.cn",
  "verification": ""    - Option
}
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X3VmNDM5YXQ3MWdlJ28qNGMyJzlyfnJpZG9+dicybDZrNydhdXcyaW82MmFsc20pdV8xcDh6aTY5bmwuNl9vKA

{
  "code": 2000,
  "data": {
    "utype": 1,
    "uid": 22,
    "uname": "lieying"
  }
}
```

----
### 2 新闻列表页

#### 2.1 列表页响应统一格式

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA

新闻：
{
  "code": 2000,
  "data": [
    {
      "nid": 16395988,     --新闻ID
      "docid": "http://mini.eastday.com/a/170421170924766.html", --用于获取评论的 docid
      "title": "61岁小卖部的最后时光 至今仍用算盘结算满满的沧桑感",  --新闻标题
      "ptime": "2017-04-26 03:10:56",  --发布时间
      "pname": "网易新闻",  --来源
      "channel": 2,        --频道
      "concern": 0,        --关心数
      "comment": 1,        --评论数
      "style": 11,         --列表图格式，新闻:0无图、1图、2、3图、11第一张大图、12第二张大图、13第三张大图；4起点号；视频样式:6小图、7、8大图, 广告样式:(广点通SDK50小图、51大图)
      "imgs": [
        "http://pro-pic.deeporiginalx.com/20170421172034b088cc388204a43060c926228dfe8f7dfa7706ec7d361969814cdcc74acf64d1_690X496.jpg",
        "http://pro-pic.deeporiginalx.com/6b9b8c0c69ecb6e5d801363e9501d376cc42b70c9c5fb8469b1a4123a3905e81.jpg",
        "http://pro-pic.deeporiginalx.com/88bbc6c047df9600f12f2b9ea470fd028070e75add3d2b55f1c35c3eb2884508.jpg"
      ],
      "rtype": 2,          --rtype类型:0普通新闻、1热点新闻、2推送新闻、3广告、4专题、5图片新闻、6视频、7本地新闻、8段子
      "icon": "https://oss-cn-hangzhou.aliyuncs.com/bdp-images/wangyixinwen163",  --新闻来源图片
      "logtype": 25,
      "logchid": 1
    },
    ...
  ]
}

视频：
{
      "nid": 16741197,
      "docid": "http://www.meipai.com/media/734827978",
      "title": "#王者荣耀激情解说##王者荣耀#阿珂：五星推荐！友情提示很难操作哦😝#游戏##搞笑#@痞子嫂💅 果哒哒你以后别用阿珂啦，你太坑啦😭",
      "ptime": "2017-04-26 04:26:53",
      "pname": "痞子范",
      "purl": "http://www.meipai.com/media/734827978",
      "channel": 44,
      "concern": 0,  --点赞次数
      "un_concern": 0,  --踩次数
      "comment": 0,
      "style": 6,
      "rtype": 6,
      "icon": "http://pro-pic.deeporiginalx.com/6a877e08c29c84816ce9a3d687393f70e07841a8fe63c1ee032c94367b8d0651.jpg",
      "videourl": "http://mvvideo1.meitudata.com/58ff345c3fc993011.mp4",  --视频播放地址
      "thumbnail": "http://mvimg10.meitudata.com/58ff30a74061e2412.jpg",  --视频显示图片
      "duration": 75,  --视频时长
      "logtype": 6,
      "logchid": 44,
      "extendData": {
        "nid": 16741197,
        "clicktimes": 0  --播放次数
      }
    }
```

----
#### 2.2 列表页刷新（广告）

_Request_

```json
POST /v2/ns/fed/ra
Host: bdp.deeporiginalx.com
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
```

| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| cid  | Long | 是        | 频道ID：推荐频道1，视频：44                 |
| scid  | Long | 否        | 二级频道ID                 |
| tcr  | Long | 是        | 起始时间，13位时间戳                         |
| tmk  | Int | 否(默认 1)  | 是(1)否(0)模拟实时发布时间(部分新闻的发布时间修改为5分钟以内) |
| p    | Long | 否(默认 1)  | 页数                                  |
| c    | Long | 否(默认 20) | 条数                                  |
| uid  | Long   | 是        | 用户ID                                |
| b    | String(base64编码) | 是         | 广告调用传的规格参数,具体见广告调用pdf,用base64编码处理|
| t    | Int    | 否(默认 0)        | 显示专题  是(1)否(0)                              |
| s    | Int    | 否(默认 0)        | 显示https图片地址  是(1)否(0)                      |
| v    | Int    | 否(默认 0)        | 显示视频  是(1)否(0)                              |
| nid  | Long | 否        | 最大新闻ID                                |
| ads  | Int | 是        | 广告来源(adsource):猎鹰广告api:1 ,广点通sdk:2 ,亦复广告api:3                              |


----
#### 2.3 列表页加载（广告）

_Request_

```json
POST /v2/ns/fed/la
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| cid  | Long | 是        | 频道ID                                |
| scid  | Long | 否        | 二级频道ID                 |
| tcr  | Long | 是        | 起始时间，13位时间戳                         |
| tmk  | Int | 否(默认 1)  | 是(1)否(0)模拟实时发布时间(部分新闻的发布时间修改为5分钟以内) |
| p    | Long | 否(默认 1)  | 页数                                  |
| c    | Long | 否(默认 20) | 条数                                  |
| uid  | Long   | 是        | 用户ID                                |
| b    | String(base64编码) | 是         | 广告调用传的规格参数,具体见广告调用pdf,用base64编码处理|
| t    | Int    | 否(默认 0)        | 显示专题  是(1)否(0)                              |
| s    | Int    | 否(默认 0)        | 显示https图片地址  是(1)否(0)                      |
| v    | Int    | 否(默认 0)        | 显示视频  是(1)否(0)                              |
| nid  | Long | 否        | 最小新闻ID                                |


----
#### 2.4 列表页刷新

_Request_

```json
GET /v2/ns/fed/rn
Host: bdp.deeporiginalx.com
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
```

| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| cid  | String | 是        | 频道ID                                |
| tcr  | String | 是        | 起始时间，13位时间戳                         |
| tmk  | String | 否(默认 1)  | 是(1)否(0)模拟实时发布时间(部分新闻的发布时间修改为5分钟以内) |
| p    | String | 否(默认 1)  | 页数                                  |
| c    | String | 否(默认 20) | 条数                                  |
| uid  | Long   | 是        | 用户ID                                |
| t    | Int    | 是        | 显示专题  是(1)否(0)                              |


----
#### 2.5 列表页加载

_Request_

```json
GET /v2/ns/fed/ln
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| cid  | String | 是        | 频道ID                                |
| tcr  | String | 是        | 起始时间，13位时间戳                         |
| tmk  | String | 否(默认 1)  | 是(1)否(0)模拟实时发布时间(部分新闻的发布时间修改为5分钟以内) |
| p    | String | 否(默认 1)  | 页数                                  |
| c    | String | 否(默认 20) | 条数                                  |
| uid  | Long   | 是        | 用户ID                                |
| t    | Int    | 是        | 显示专题  是(1)否(0)                              |

----

#### 2.6 行政区划-列表页刷新

_Request_

```json
GET /v2/ns/loc/r
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须     | 参数解释                                     |
| ---- | :----- | :------- | :--------------------------------------- |
| pr   | String | 否        | 省，<font color="#ff0000">三种行政区划参数最少提供一个，不能为空字符串[""]</font> |
| di   | String | 否        | 市                                        |
| ci   | String | 否        | 区/县                                      |
| tcr  | String | 是        | 起始时间，13位时间戳                              |
| tmk  | String | 否(默认 1)  | 是(1)否(0)模拟实时发布时间(部分新闻的发布时间修改为5分钟以内)      |
| p    | String | 否(默认 1)  | 页数                                       |
| c    | String | 否(默认 20) | 条数                                       |

----
#### 2.7 行政区划-列表页加载

_Request_

```json
GET /v2/ns/loc/l
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须     | 参数解释                                     |
| ---- | :----- | :------- | :--------------------------------------- |
| pr   | String | 否        | 省，<font color="#ff0000">三种行政区划参数最少提供一个，不能为空字符串[""]</font> |
| di   | String | 否        | 市                                        |
| ci   | String | 否        | 区/县                                      |
| tcr  | String | 是        | 起始时间，13位时间戳                              |
| tmk  | String | 否(默认 1)  | 是(1)否(0)模拟实时发布时间(部分新闻的发布时间修改为5分钟以内)      |
| p    | String | 否(默认 1)  | 页数                                       |
| c    | String | 否(默认 20) | 条数                                       |

----
#### 2.8 新闻源-列表页刷新

_Request_

```json
GET /v2/ns/src/r
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| sid  | String | 是        | 新闻源ID                               |
| tcr  | String | 是        | 起始时间，13位时间戳                         |
| tmk  | String | 否(默认 1)  | 是(1)否(0)模拟实时发布时间(部分新闻的发布时间修改为5分钟以内) |
| p    | String | 否(默认 1)  | 页数                                  |
| c    | String | 否(默认 20) | 条数                                  |

----
#### 2.9 新闻源-列表页加载

_Request_

```json
GET /v2/ns/src/l
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| sid  | String | 是        | 新闻源ID                               |
| tcr  | String | 是        | 起始时间，13位时间戳                         |
| tmk  | String | 否(默认 1)  | 是(1)否(0)模拟实时发布时间(部分新闻的发布时间修改为5分钟以内) |
| p    | String | 否(默认 1)  | 页数                                  |
| c    | String | 否(默认 20) | 条数                                  |


----
### 3 新闻详情页

#### 3.1 新闻详情内容

_Request_

```json
GET /v2/ns/con
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须          | 参数解释 |
| ---- | :----- | :------------ | :--- |
| nid  | String | 是             | 新闻ID |
| uid  | String | 否(非正式注册用户不提供) | 用户ID |
| s    | Int    | 否(默认 0)        | 显示https图片地址  是(1)否(0)                      |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": {
    "nid": 6825,                                    - 新闻ID
    "docid": "http://mp.weixin.qq.com/...",         - 用于获取评论的docid
    "title": "分享 | 驾照自学直考到底难不难？成都31人报名2人拿证，他们的经验是......",
    "ptime": "2016-05-22 01:03:00",                 - 发布时间
    "pname": "央视新闻",
    "purl": "http://mp.weixin.qq.com/s?...",
    "channel": 2,                                   - 频道ID
    "inum": 6,                                      - 正文图片数量
    "tags": [                                       - 关键字
      "驾照","驾校"
    ],
    "descr": "今年4月1日起，武汉、成都、南京、福州等...",
    "content": [
      {
        "txt": "本文来源：荆楚网、华西都市报"
      },
      {
        "img": "http://bdp-pic.deeporiginalx.com/W0JAMjIzZmZhZGQ.jpg"
      },
      {
        "vid": "http://anyvideourl.com"
      },
      ...
    ],
    "collect": 1,
    "concern": 1,
    "comment": 0,
	"colflag":1,		- 是(1)否(0)已收藏
	"conflag":1,		- 是(1)否(0)已关心
	"conpubflag":1		- 是(1)否(0)已关心该新闻对应的发布源
  }
}
```


----
#### 3.2 下一条详情页

_Request_

```json
GET /v2/ns/next
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须          | 参数解释 |
| ---- | :----- | :------------ | :--- |
| nid  | Long | 是              | 新闻ID |
| uid  | Long | 是              | 用户ID |
| chid | Long | 是              | 频道ID |
| s    | Int  | 否(默认 0)       | 显示https图片地址  是(1)否(0) |

----
#### 3.3 上一条详情页

_Request_

```json
GET /v2/ns/last
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须          | 参数解释 |
| ---- | :----- | :------------ | :--- |
| nid  | Long | 是              | 新闻ID |
| uid  | Long | 是              | 用户ID |
| chid | Long | 是              | 频道ID |
| s    | Int  | 否(默认 0)       | 显示https图片地址  是(1)否(0) |

----

----
#### 3.4 视频详情页

_Request_

```json
GET /v2/vi/con
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须          | 参数解释 |
| ---- | :----- | :------------ | :--- |
| nid  | Long | 是             | 新闻ID |
| uid  | Long | 否              | 用户ID |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": {
    "nid": 6825,                                    - 新闻ID
    "docid": "http://mp.weixin.qq.com/...",         - 用于获取评论的docid
    "title": "分享 | 驾照自学直考到底难不难？成都31人报名2人拿证，他们的经验是......",
    "ptime": "2016-05-22 01:03:00",                 - 发布时间
    "pname": "央视新闻",
    "purl": "http://mp.weixin.qq.com/s?...",
    "channel": 2,                                   - 频道ID
    "inum": 6,                                      - 正文图片数量
    "tags": [                                       - 关键字
      "驾照","驾校"
    ],
    "descr": "今年4月1日起，武汉、成都、南京、福州等...",
    "content": [
      {
        "txt": "本文来源：荆楚网、华西都市报"
      },
      {
        "img": "http://bdp-pic.deeporiginalx.com/W0JAMjIzZmZhZGQ.jpg"
      },
      {
        "vid": "http://anyvideourl.com"
      },
      ...
    ],
    "collect": 1,
    "concern": 1,
    "comment": 0,
	"colflag":1,		- 是(1)否(0)已收藏
	"conflag":1,		- 是(1)否(0)已关心
	"conpubflag":1,		- 是(1)否(0)已关心该新闻对应的发布源
	"videourl": "http://gslb.miaopai.com/stream/sHLX8Z6pr0hAKyK6SDucXA__.mp4?yx=&refer=weibo_app&Expires=1482375610&ssig=MtRxNEvf0N&KID=unistore,video", -视频url
    "thumbnail": "http://bdp-pic.deeporiginalx.com/W0JAMjIzZmZhZGQ.jpg" -背景图
  }
}
```

----
#### 3.5 详情页相关推荐列表

_Request_

```json
GET /v2/ns/asc
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须     | 参数解释 |
| ---- | :----- | :------- | :--- |
| nid  | Long | 是        | 新闻ID |
| s    | Int    | 否(默认 0)        | 显示https图片地址  是(1)否(0)  |
| p    | Long | 否(默认 1)  | 页数   |
| c    | Long | 否(默认 20) | 条数   |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": [
    {
      "url": "http://deeporiginalx.com/news.html?type=0&nid=10608629",
      "title": "【背部训练】与@韩夕Jessie 的背部训练。来一起练背￼一介粗人的秒拍视频  workout vlog vol.3",
      "from": "Qidian",
      "rank": 1,
      "pname": "微博热点",
      "ptime": "2017-01-08 22:02:26",
      "img": "http://wsqncdn.miaopai.com/stream/Uj1lz71h-04mAv15B33TJA___m.jpg",
      "nid": 10608629,
      "duration": 190,
      "logtype": 26,
      "logchid": 0
    },
    ...
  ]
}
```
#### 3.6 详情页相关推荐列表(带广告)

_Request_

```json
POST /v2/ns/ascad
Host: bdp.deeporiginalx.com
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
```

| Key  | 参数类型   | 是否必须     | 参数解释 |
| ---- | :----- | :------- | :--- |
| nid  | Long | 是        | 新闻ID |
| b    | String(base64编码) | 是         | 广告调用传的规格参数,具体见广告调用pdf,用base64编码处理|
| s    | Int    | 否(默认 0)        | 显示https图片地址  是(1)否(0)  |
| p    | Long | 否(默认 1)  | 页数   |
| c    | Long | 否(默认 20) | 条数   |
| ads  | Int | 是        | 广告来源(adsource):猎鹰广告api:1 ,广点通sdk:2 ,亦复广告api:3                              |    

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": [
    {
      "url": "http://deeporiginalx.com/news.html?type=0&nid=10608629",
      "title": "【背部训练】与@韩夕Jessie 的背部训练。来一起练背￼一介粗人的秒拍视频  workout vlog vol.3",
      "from": "Qidian",
      "rank": 1,
      "pname": "微博热点",
      "ptime": "2017-01-08 22:02:26",
      "img": "http://wsqncdn.miaopai.com/stream/Uj1lz71h-04mAv15B33TJA___m.jpg",
      "nid": 10608629,
      "duration": 190,
      "logtype": 26,
      "logchid": 0
    },
    {
      "url": "",
      "title": "",
      "from": "",
      "rank": 0,
      "pname": "",
      "ptime": "2017-03-28 18:15:10",
      "rtype": 3,
      "logtype": 3,
      "adresponse": {
        "version": 1,
        "status": 0,
        "message": "Success",
        "data": {
          "adspace": [
            {
              "aid": 246,
              "adformat": 5,
              "creative": [
                {
                  "cid": 0,
                  "index": 0,
                  "ad_native": [
                    {
                      "template_id": "167",
                      "index": 0,
                      "required_field": 2,
                      "action_type": 0,
                      "required_value": "http://pgdt.gtimg.cn/gdt/0/DAABYMtAEsAEsAAIBYHBf6BTpLlzwG.jpg/0?ck=59b912f913a8bda4c9b6cbede441ba99",
                      "type": "jpg",
                      "index_value": "icon"
                    },
                    {
                      "template_id": "167",
                      "index": 1,
                      "required_field": 1,
                      "action_type": 0,
                      "required_value": "会员才知道的世界，你懂的",
                      "type": "text",
                      "index_value": "title"
                    },
                    {
                      "template_id": "167",
                      "index": 2,
                      "required_field": 1,
                      "action_type": 0,
                      "required_value": "95%的人不知道的优惠！你正在浪费你的特权",
                      "type": "text",
                      "index_value": "description"
                    },
                    {
                      "template_id": "167",
                      "index": 3,
                      "required_field": 2,
                      "action_type": 0,
                      "required_value": "http://pgdt.gtimg.cn/gdt/0/transformer_11383441592749851919_149.jpg/0?ck=f0274540034de43753d2084ead861db9",
                      "type": "jpg",
                      "index_value": "image"
                    }
                  ],
                  "impression": [
                    "http://v.gdt.qq.com/gdt_stats.fcg?count=1&viewid0=88_e5tPeEmHlT!TyRJImpjTR!tw7SW2ZKbEK1Fhww8fFIN9G5GICvcCfZWqZo8bLGxwMAeermn!wIyR5DQksPyz5pz2KCQUP0E5IuS_rVAac19hqy5PL1nx7GoVi47mS7nOuciGaNBXMAAInLntjyCJjRDKNQY1o6BNQdhm2n3UU6unRpwPAU8iYKvl6yhubR9e7Ky1xP2H0JzXNdRUC0g",
                    "http://as.lieying.cn/v2/forward/imp/ch/16?version=1.0&sspaid=246&sid=0&guid=ffa8568e69c74e61910f16a293467776"
                  ],
                  "click": [
                    "http://as.lieying.cn/v2/forward/click/ch/16?version=1.0&sspaid=246&sid=0&guid=ffa8568e69c74e61910f16a293467776"
                  ],
                  "event": [
                    {
                      "event_key": 1,
                      "event_value": "http://c.gdt.qq.com/gdt_mclick.fcg?viewid=88_e5tPeEmHlT!TyRJImpjTR!tw7SW2ZKbEK1Fhww8fFIN9G5GICvcCfZWqZo8bLGxwMAeermn!wIyR5DQksPyz5pz2KCQUP0E5IuS_rVAac19hqy5PL1nx7GoVi47mS7nOuciGaNBXMAAInLntjyCJjRDKNQY1o6BNQdhm2n3UU6unRpwPAU8iYKvl6yhubR9e7Ky1xP2H0JzXNdRUC0g&jtype=0&i=1&os=2&acttype=&s=%7B%22down_x%22%3A-999%2C%22down_y%22%3A-999%2C%22up_x%22%3A-999%2C%22up_y%22%3A-999%7D"
                    }
                  ],
                  "admark": "http://alicdn.lieying.cn/sentshow/imgs/tmp/tsa_ad_logo.png"
                }
              ]
            }
          ]
        }
      }
    }
    ...
  ]
}

```


----
### 4 评论列表
----
#### 4.1 新闻普通评论列表

_Request_

```json
GET /v2/ns/coms/c
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型           | 是否必须     | 参数解释                               |
| ---- | :------------- | :------- | :--------------------------------- |
| did  | String(base64) | 是        | 新闻 docid                           |
| uid  | String         | 否        | 注册用户ID，提供该ID会在响应中设置该用户的点赞标记 upflag |
| p    | String         | 否(默认 1)  | 页数                                 |
| c    | String         | 否(默认 20) | 条数                                 |

_Response_

```json
HTTP/1.1 200 OKT
Content-Type: application/json

{
  "code": 2000,
  "data": [
    {
      "id": 2,                          - 评论ID
      "content": "66666",               - 评论正文
      "commend": 10,                    - 赞数
      "ctime": "2016-05-24 19:22:11",   - 创建时间
      "uid": 4,                         - 创建该评论的用户ID
      "uname": "zhange",                - 创建该评论的用户名
      "avatar": "http://touxiang.jpg"   - Option
      "docid": "http://toutiao.com/group/2223/comments/111",  - 该评论对应的新闻 docid
      "upflag": 1,                      - 用户是否能对该条评论点赞，0、1 对应 可点、不可点
      "nid": 12332,						- 该评论的新闻ID，仅用户个人评论列表接口
      "ntitle": "这是新闻标题"			- 该评论的新闻标题，仅用户个人评论列表接口
    }
  ]
}
```

----

#### 4.2 新闻热点评论列表

_Request_

```json
GET /v2/ns/coms/h
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型           | 是否必须    | 参数解释                               |
| ---- | :------------- | :------ | :--------------------------------- |
| did  | String(base64) | 是       | 新闻 docid                           |
| uid  | String         | 否       | 注册用户ID，提供该ID会在响应中设置该用户的点赞标记 upflag |
| p    | String         | 否(默认 1) | 页数                                 |
| c    | String         | 否(默认 5) | 条数                                 |



----
### 5 专题详情

_Request_

```json
GET /v2/ns/tdq
Host: bdp.deeporiginalx.com
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
```

| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| tid  | Int       | 是          | 专题ID                                 |


_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": {
    "topicBaseInfo": {                                                 - 专题基本信息
      "id": 1,                                                         - 专题ID
      "name": "专题标题",
      "cover": "http://bdp-pic.deeporiginalx.com/W0JAMjM4Mjc4ZDE.png", - 专题封面图
      "description": "",
      "class_count": 0,                                                - 专题包含分类数
      "news_count": 0,                                                 - 专题包含新闻数
      "online": 1,                                                     - 专题是否上线
      "top": 0,                                                        - 专题是否置顶
      "create_time": "2016-09-27 16:46:54"
    },
    "topicClass": [                                                    - 专题分类数组
      {
        "topicClassBaseInfo": {                                        - 专题分类基本信息
          "id": 1,
          "name": "分类一",
          "topic": 1,
          "order": 1
        },
        "newsFeed": [                                                  - 专题分类中的新闻feed流数组
          {
            "nid": 7660914,
            "docid": "http://www.yidianzixun.com/article/0Ehfu9ep",
            "title": "詹姆斯有多强？NBA经理们说最强SF/PF都是他！",
            "ptime": "2016-10-19 11:23:59",
            "pname": "颜小白的篮球梦",
            "channel": 6,
            "collect": 0,
            "concern": 0,
            "comment": 0,
            "style": 1,
            "imgs": [
              "http://bdp-pic.deeporiginalx.com/111c7c64f9ccb7be3c8a61e6d3fcca0e_544X408.jpg"
            ],
            "logtype": 41,                                             - 推荐日志类型:比rtype区分更细
            "logchid": 1                                              - 点击新闻所在频道:区分奇点和其他频道
          }
          ......
        ]
      },
      {
        "topicClassBaseInfo": {
          "id": 2,
          "name": "分类二",
          "topic": 1,
          "order": 2
        },
        "newsFeed": [
          {
            "nid": 7660911,
            "docid": "https://kuaibao.qq.com/s/20161019G02RTQ00",
            "title": "2016学年度华东师范大学优秀外国留学生奖学金申请",
            "ptime": "2016-10-19 11:23:53",
            "pname": "华东师范大学留学生办公室",
            "channel": 28,
            "collect": 0,
            "concern": 0,
            "comment": 0,
            "style": 0,
            "city": "上海",
            "logtype": 41,                                             - 推荐日志类型:比rtype区分更细
            "logchid": 1                                              - 点击新闻所在频道:区分奇点和其他频道
          }
          ......
        ]
      }
      ......
    ]
  }
}
```



----
### 6 新闻搜索

----
#### 6.1 搜索
_Request_

```json
GET /v2/ns/es/s
Content-Type: application/json
Host: bdp.deeporiginalx.com
```
| Key      | 参数类型   | 是否必须     | 参数解释  |
| -------- | :----- | :------- | :---- |
| keywords | String | 是        | 搜索关键字 |
| uid      | Long   | 否        | 用户id |
| p        | Long   | 否(默认 1)  | 页数    |
| c        | Long   | 否(默认 20) | 条数    |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": [
    {
      "nid": 6695,                                            - 新闻ID
      "docid": "http://toutiao.com",                          - 用于获取评论的 docid
      "title": "日媒：欧洲能源多元化 俄天然气外交走向终结",
      "ptime": "2016-05-22 01:20:46",
      "pname": "参考消息",
      "purl": "http://m.cankaoxiaoxi.com//20160522/1166670.shtml",
      "channel": 9,
      "collect": 0,                                           - 收藏数
      "concern": 0,                                           - 关心数
      "comment": 4,                                           - 评论数
      "style": 1,                                             - 列表图格式，0、1、2、3
      "imgs": [                                               - 该字段会有对应style数值的图片
        "http://bdp-pic.deeporiginalx.com/W0JAMjM4Mjc4ZDE.png"
      ],
      "province": "山东",
      "city": "青岛",
      "district": "山东"
    },
    …
  ],
  "total": 2000                                               - 总条数
}
```
----
#### 6.2 搜索新闻及订阅号
_Request_

```json
GET /v2/ns/es/snp
Content-Type: application/json
Host: bdp.deeporiginalx.com
```
| Key      | 参数类型   | 是否必须     | 参数解释  |
| -------- | :----- | :------- | :---- |
| keywords | String | 是        | 搜索关键字 |
| p        | Long   | 否(默认 1)  | 页数    |
| c        | Long   | 否(默认 20) | 条数    |
| uid      | Long   | 否        | 用户id  |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": 
  {
            "news": [
                {
                  "nid": 6695,                                            - 新闻ID
                  "docid": "http://toutiao.com",                          - 用于获取评论的 docid
                  "title": "日媒：欧洲能源多元化 俄天然气外交走向终结",
                  "ptime": "2016-05-22 01:20:46",
                  "pname": "参考消息",
                  "purl": "http://m.cankaoxiaoxi.com//20160522/1166670.shtml",
                  "channel": 9,
                  "collect": 0,                                           - 收藏数
                  "concern": 0,                                           - 关心数
                  "comment": 4,                                           - 评论数
                  "style": 1,                                             - 列表图格式，0、1、2、3
                  "imgs": [                                               - 该字段会有对应style数值的图片
                    "http://bdp-pic.deeporiginalx.com/W0JAMjM4Mjc4ZDE.png"
                  ],
                  "province": "山东",
                  "city": "青岛",
                  "district": "山东"
                },
                …
              ],
              "total": 2000,                                              - 总条数
              "publisher": [
                  {
                    "id": 6695,                                            - 订阅号ID
                    "ctime": "2016-05-22 01:20:46",                        - 创建时间
                    "name": "安卓中国",                                     -  订阅号名称
                    "concern": 0,                                           - 关注数
                    "flag": 0,                                             - 0未关注, >0已关注
                  },
                  …
                ]
  }
}
```

### 7 广告请求

_Request_

```json
POST /v2/ns/ad
Host: bdp.deeporiginalx.com
```
| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| uid  | Long   | 是        | 用户ID                                |
| b    | String(base64编码) | 是         | 广告调用传的规格参数,具体见广告调用pdf,用base64编码处理|
| s    | Int  | 否(默认 0)       | 显示https图片地址  是(1)否(0) |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": [
    {
      "nid": 0,
      "docid": "0",
      "title": "走！去滑雪！满200减100",
      "ptime": "2017-01-18 14:20:02",
      "pname": " ",
      "purl": "http://c.gdt.qq.com/gdt_mclick.fcg?viewid=3zeyBuN!Sfr0OTbn_JziawNBUXB9a189HufqCn3tppKE3PlycbsBypCBuq1VTWife3JAyQ0BjqfDDuv4IzGOSBzYLg9fnz6AQwPhwcHhVhk2AT_YiIYHU_L0wfeztXsagWv1L_yl!JvhiXz0L_IQ_Sgk8m31agR_Z4Xr2HfcXFAt7R40F3s2cUFaHmLg!N4paGC1WWDCHv9Oi9aCQKXpxHtqiQ_e2yZi&jtype=0&i=1&os=2&acttype=&s={\"down_x\":-999,\"down_y\":-999,\"up_x\":-999,\"up_y\":-999}",
      "channel": 9999,
      "collect": 0,
      "concern": 0,
      "comment": 0,
      "style": 1,
      "imgs": [
        "https://pgdt.gtimg.cn/gdt/0/DAABYMtAUAALQABXBYfsxzAU7tY5EH.jpg/0?ck=b497aed2f491d4768f106639edfc2fe0"
      ],
      "rtype": 3,
      "adimpression": [
        "http://v.gdt.qq.com/gdt_stats.fcg?count=1&viewid0=3zeyBuN!Sfr0OTbn_JziawNBUXB9a189HufqCn3tppKE3PlycbsBypCBuq1VTWife3JAyQ0BjqfDDuv4IzGOSBzYLg9fnz6AQwPhwcHhVhk2AT_YiIYHU_L0wfeztXsagWv1L_yl!JvhiXz0L_IQ_Sgk8m31agR_Z4Xr2HfcXFAt7R40F3s2cUFaHmLg!N4paGC1WWDCHv9Oi9aCQKXpxHtqiQ_e2yZi"
      ]
    }
  ]
}
```

----
### 8 新闻发布源

----

#### 8.1 添加新闻发布源关心

*Request*

```
POST /v2/ns/pbs/cocs
Authorization: Basic MmFhOXhrZTlxbGVmM3luOCc2M3kwanFwcChjeHBmczM1ZDRjYip4cyoycjdobG51ZWd5eXFmOGZiaHRrcTVrcw
X-Requested-With: *
Content-Type: application/json
Host: bdp.deeporiginalx.com
```

| Key   | 参数列表   | 是否必须 | 参数解释  |
| ----- | ------ | ---- | ----- |
| uid   | String | 是    | 用户ID  |
| pname | String | 是    | 发布源名称 |

*Response*

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code":2000,
  "data":122			- 更新后，该发布源的关心数
}
```

#### 8.2 取消新闻发布源关心

*Request*

```
DELETE /v2/ns/pbs/cocs
Authorization: Basic MmFhOXhrZTlxbGVmM3luOCc2M3kwanFwcChjeHBmczM1ZDRjYip4cyoycjdobG51ZWd5eXFmOGZiaHRrcTVrcw
X-Requested-With: *
Content-Type: application/json
Host: bdp.deeporiginalx.com
```

| Key   | 参数列表   | 是否必须 | 参数解释  |
| ----- | ------ | ---- | ----- |
| uid   | String | 是    | 用户ID  |
| pname | String | 是    | 发布源名称 |

*Response*

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code":2000,
  "data":122			- 更新后，该发布源的关心数
}
```

#### 8.3 已关心发布源列表

*Request*

```
GET /v2/ns/pbs/cocs?uid=112011 HTTP/1.1
Authorization: Basic MmFhOXhrZTlxbGVmM3luOCc2M3kwanFwcChjeHBmczM1ZDRjYip4cyoycjdobG51ZWd5eXFmOGZiaHRrcTVrcw
Host: bdp.deeporiginalx.com
```

| Key  | 参数列表   | 是否必须 | 参数解释 |
| ---- | ------ | ---- | ---- |
| uid  | String | 是    | 用户ID |

*Response*

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code":2000,
  "data":[
    {
      "id":6,
      "ctime":"2016-07-18 22:09:19",
      "name":"深圳吃货",
      "icon":"http://some.png",
      "descr":"深圳美食",
      "concern":1
    }
  ]
}
```

#### 8.4 已关心发布源的新闻列表刷新

*Request*

```
GET /v2/ns/pbs/cocs/r
Authorization: Basic MmFhOXhrZTlxbGVmM3luOCc2M3kwanFwcChjeHBmczM1ZDRjYip4cyoycjdobG51ZWd5eXFmOGZiaHRrcTVrcw
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须     | 参数解释        |
| ---- | ------ | -------- | ----------- |
| uid  | String | 是        | 用户ID        |
| tcr  | String | 是        | 起始时间，13位时间戳 |
| p    | String | 否(默认 1)  | 页数          |
| c    | String | 否(默认 20) | 条数          |

**Response：新闻列表页数据格式**

#### 8.5 已关心发布源的新闻列表加载

*Request*

```
GET /v2/ns/pbs/cocs/l
Authorization: Basic MmFhOXhrZTlxbGVmM3luOCc2M3kwanFwcChjeHBmczM1ZDRjYip4cyoycjdobG51ZWd5eXFmOGZiaHRrcTVrcw
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须     | 参数解释        |
| ---- | ------ | -------- | ----------- |
| uid  | String | 是        | 用户ID        |
| tcr  | String | 是        | 起始时间，13位时间戳 |
| p    | String | 否(默认 1)  | 页数          |
| c    | String | 否(默认 20) | 条数          |

**Response：新闻列表页数据格式**

#### 8.6 指定新闻发布源新闻列表

*Request*

```
GET /v2/ns/pbs
Host: bdp.deeporiginalx.com
```

| Key   | 参数类型   | 是否必须     |                     |
| ----- | ------ | -------- | ------------------- |
| pname | String | 是        | 发布源名称               |
| info  | String | 否(默认 0)  | 是(1)否(0)同时获得改发布源的详情 |
| tcr   | String | 是        | 起始时间，13位时间戳         |
| p     | String | 否(默认 1)  | 页数                  |
| c     | String | 否(默认 20) | 条数                  |

*Response*

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code":2000,
  "data":{
    "info":{							- 该发布源详情
      "id":4,
      "ctime":"2016-07-15 18:23:05",
      "name":"环球网",
      "concern":1,
      "icon":"http://some.png",
      "descr":"环球网新闻"
    },
  	"news":[
      {
       "nid":5212751,					- 通用列表页结构
       "docid":"http://m.huanqiu...",
       "title":" “十二金衩”+“彩蛋”:盛大游戏Showgirl制霸2016CJ",
       "ptime":"2016-07-22 13:47:45",
       "pname":"环球网",
       "purl":"http://m.huanqiu....",
       "channel":22,
       "collect":0,
       "concern":0,
       "comment":0,
       "style":3,
       "imgs":["http://bdp-pic.deeporiginalx.com/...",
               "http://bdp-pic.deeporiginalx.com/..."
              ]
      },
      ...
    ]
  }
```

----
### 9 用户相关操作
----
#### 9.1 新闻评论

----
##### 9.1.1 创建评论

_Request_

```json
POST /v2/ns/coms
Content-Type: application/json
Host: bdp.deeporiginalx.com
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
X-Requested-With: *

{
  "content": "66666",
  "commend": 0,                             - 设置为 0
  "ctime": "2016-05-24 19:22:11",
  "uid": 22,
  "uname": "zhange",
  "avatar": "http://customtouxiang.jpg"     - Option
  "docid": "http://toutiao.com/group/1/comments/?count=100&offset=0&format=json"
}
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA

{
  "code": 2000,
  "data": 7               - 已创建的评论ID
}
```

----
##### 9.1.2 删除评论

_Request_

```json
DELETE /v2/ns/au/coms
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
X-Requested-With: *
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型           | 是否必须 | 参数解释      |
| ---- | :------------- | :--- | :-------- |
| did  | String(base64) | 是    | 评论的 docid |
| cid  | String         | 是    | 评论  ID    |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA

{
  "code": 2000,
  "data": 7               - 已删除的评论ID
}
```

----
##### 9.1.3 查看评论列表

_Request_

```json
GET /v2/ns/au/coms
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须 | 参数解释 |
| ---- | :----- | :--- | :--- |
| uid  | String | 是    | 用户ID |

#### 9.2 新闻评论点赞

----
##### 9.2.1 点赞

_Request_

```json
POST /v2/ns/coms/up
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
X-Requested-With: *
Content-Type: application/json
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须 | 参数解释 |
| ---- | :----- | :--- | :--- |
| cid  | String | 是    | 评论ID |
| uid  | String | 是    | 用户ID |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA

{
  "code": 2000,
  "data": 12            - 更新后的赞数
}
```

----
##### 9.2.2 取消赞

_Request_

```json
DELETE /v2/ns/coms/up
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
X-Requested-With: *
Content-Type: application/json
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须 | 参数解释 |
| ---- | :----- | :--- | :--- |
| cid  | String | 是    | 评论ID |
| uid  | String | 是    | 用户ID |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA

{
  "code": 2000,
  "data": 12            - 更新后的赞数
}
```

#### 9.3 新闻收藏

----
##### 9.3.1 添加收藏

_Request_

```json
POST /v2/ns/cols
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
X-Requested-With: *
Content-Type: application/json
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须 | 参数解释 |
| ---- | :----- | :--- | :--- |
| nid  | String | 是    | 新闻ID |
| uid  | String | 是    | 用户ID |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA

{
  "code": 2000,
  "data": 2               - 更新后的收藏数
}
```

----
##### 9.3.2 取消收藏

_Request_

```json
DELETE /v2/ns/cols
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
X-Requested-With: *
Content-Type: application/json
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须 | 参数解释 |
| ---- | :----- | :--- | :--- |
| nid  | String | 是    | 新闻ID |
| uid  | String | 是    | 用户ID |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA

{
  "code": 2000,
  "data": 1               - 更新后的收藏数
}
```

----
##### 9.3.3 查看收藏列表

_Request_

```json
GET /v2/ns/au/cols
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
X-Requested-With: *
Content-Type: application/json
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须 | 参数解释 |
| ---- | :----- | :--- | :--- |
| uid  | String | 是    | 用户ID |

_Response：新闻列表页数据格式_
返回格式与feed流一样

#### 9.4 新闻关心

----
##### 9.4.1 添加关心

_Request_

```json
POST /v2/ns/cocs
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
X-Requested-With: *
Content-Type: application/json
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须 | 参数解释 |
| ---- | :----- | :--- | :--- |
| nid  | String | 是    | 新闻ID |
| uid  | String | 是    | 用户ID |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA

{
  "code": 2000,
  "data": 2               - 更新后的关心数
}
```

----
##### 9.4.2 取消关心

_Request_

```json
DELETE /v2/ns/cocs
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
X-Requested-With: *
Content-Type: application/json
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须 | 参数解释 |
| ---- | :----- | :--- | :--- |
| nid  | String | 是    | 新闻ID |
| uid  | String | 是    | 用户ID |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA

{
  "code": 2000,
  "data": 1               - 更新后的关心数
}
```

----
##### 9.4.3 查看关心列表

_Request_

```json
GET /v2/ns/au/cocs
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
X-Requested-With: *
Content-Type: application/json
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须 | 参数解释 |
| ---- | :----- | :--- | :--- |
| uid  | String | 是    | 用户ID |

_Response_


----
### 10 新闻频道

----
#### 10.1 普通频道

_Request_

```json
GET /v2/ns/chs
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须    | 参数解释                  |
| ---- | :----- | :------ | :-------------------- |
| s    | String | 否(默认 1) | 上线状态，0 或 1            |
| sech | String | 否(默认 0) | 是否同时获得每个一级频道对应的二级频道列表 |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": [
    {
      "id": 11,
      "cname": "热点",
      "state": 1
      "schs": [				- 如果提供了 sech 参数，并且该一级频道拥有二级频道
  		"id":1,				- 二级频道 ID
      	"cname":"评测",
      	"chid":11,			- 对应的一级频道 ID
      	"state":1
	  ]
    }
  ]
}
```

----
#### 10.2 用户定制频道

_Request_

```json
GET /v2/ns/au/chs/{uid}
Host: bdp.deeporiginalx.com
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
X-Requested-With: *
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA

{
  "code": 2000,
  "data": [
    {
      "id": 2,
      "cname": "社会",
      "state": 1
    },
    ...
  ]
}
```

----
#### 10.3 用户定制频道修改

_Request_

```json
POST /v2/ns/au/chs/{uid}
Host: bdp.deeporiginalx.com
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
X-Requested-With: *

{
  "channels": [
    "娱乐",
    ...
  ]
}
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA

{
  "code": 2000,
  "data": {
    "channels": [
      "娱乐",
      ...
    ]
  }
}
```


----
### 11 日志上报

----
#### 11.1 点击日志

_Request_

```json
GET /rep/v2/c
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须 | 参数解释 |
| ---- | :----- | :--- | :--- |
| u    | Long   | 是    | 用户ID |
| p    | String | 否    | 省份   |
| t    | String | 否    | 市    |
| i    | String | 否    | 地区/县 |
| d    | String | 是    | 数据   |

字段 d 的数据格式初始为JSON，通过base64加密并移除末尾空格：

```json
"d":[
  {
    "n": 234,   - Long 新闻ID
    "c": 12,    - Int 频道ID
    "t": 2,     - Int 新闻类型ID
    "s": 23,    - Int 停留时长，秒
    "f": 1,     - Int 进入位置ID
    "lt": 0,    - Int logtype,新闻推荐类型
    "lc": 1,    - Int logchid,新闻从那个频道点击进入
  },
  ...
]
```

1. 将字段d的值(不包括字段名[“d”:]部分)进行base64加密 - ImQiOlt7Im4iOiAyMzQsImMiOiAxMiwidCI6IDIsInMiOiAyMywiZiI6IDF9XQ==
2. 移除空格 - ImQiOlt7Im4iOiAyMzQsImMiOiAxMiwidCI6IDIsInMiOiAyMywiZiI6IDF9XQ
3. 与其他参数一起进行URLEncode，构建请求出URL

**示例**

```sh
GET http://bdp.deeporiginalx.com/rep/v2/c?u=22&p=%E5%8C%97%E4%BA%AC&t=%E5%8C%97%E4%BA%AC&i=%E4%B8%9C%E5%9F%8E&d=ImQiOlt7Im4iOiAyMzQsImMiOiAxMiwidCI6IDIsInMiOiAyMywiZiI6IDF9XQ

HTTP/1.1 200 OK
Content-Type: image/gif
```

<font color="#ff8000">目前“新闻类型ID”尚未提供，暂时设置为以下值，后续相应模块开发后，根据实际的值提供：</font>

**新闻类型ID**

| 值    | 说明   |
| ---- | :--- |
| 0    | 普通新闻 |
| 1    | 个性推荐 |
| 2    | 热点推荐 |
| 3    | 编辑推荐 |
| 4    | 广告推广 |
| -    | 后续新增 |

**进入位置ID**

| 值    | 说明   |
| ---- | :--- |
| 0    | 列表页  |
| 1    | 搜索页  |
| -    | 后续新增 |

----

----
#### 11.2 用户手机信息,手机app列表收集接口

_Request_

```json

POST /v2/au/app
Host: bdp.deeporiginalx.com
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
Content-Type: application/json

{
  "uid": 634788,
  "province": "省份",
  "city": "市",
  "area": "地区/县",
  "brand": "苹果",
  "model": "iPhone7",
  "apps": [
    {
      "app_id": "sogou.mobile.explorer",
      "app_name": "搜狗浏览器",
      "active": 0                                   --预装软件
    },
    {
      "app_id": "com.baidu.BaiduMap",
      "app_name": "百度地图",
      "active": 1                                   --自装软件
    }
  ]
}
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": 634788
}
```

----
#### 11.3 转发记录

_Request_

```json
POST /v2/ns/replay
Content-Type: application/json
Host: bdp.deeporiginalx.com
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
X-Requested-With: *

{
  "nid": 1,                                                              - 新闻ID
  "uid": 634788,                                                         - 用户ID
  "whereabout": 1                                                        - 转发去向:1:微信朋友圈 2:微信好友 3:QQ好友 4:新浪微博 5:短信 6:邮件 7:转发链接
}
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA

{
  "code": 2000,
  "data": 1               - 已创建的转发记录ID
}
```

----
#### 11.4 不感兴趣记录

_Request_

```json
POST /v2/ns/hate
Content-Type: application/json
Host: bdp.deeporiginalx.com
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
X-Requested-With: *

{
  "nid": 1,                                                              - 新闻ID
  "uid": 634788,                                                         - 用户ID
  "reason": 1                                                            - 不感兴趣原因(可空):1、不喜欢 2、低质量 3、重复旧闻 4、来源;
}
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA

{
  "code": 2000,
  "data": 1               - 已创建的不感兴趣记录ID
}
```


#### 11.5 滑动接口

_Request_

```json
GET /v2/sl/ins
Host: bdp.deeporiginalx.com
```
| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| mid  | Stirng   | 是        | 机器唯一id, Android为imei, IOS为idfa                   |
| uid  | Long   | 是        | 用户ID                                |
| ctype  | Int   | 是        | 渠道类型, 1：奇点资讯， 2：黄历天气，3：纹字锁频，4：猎鹰浏览器，5：白牌  |
| ptype    | Int | 是         | 平台类型，1：IOS，2：安卓，3：网页，4：无法识别|
| version_text  | Stirng   | 是        | APP版本    |
| operate_type  | Int   | 是        | 操作类型: 滑动展示:1， 广告展示：2    |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": 634788   --uid
}
```

### 12 广告接口

#### 12.1 获取广告展示平台接口

_Request_

```json
POST /v2/ad/source
Host: bdp.deeporiginalx.com
```
| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| uid  | Long   | 是        | 用户ID                                |
| did  | String   | 是        | 设备标识ID                                |
| ctype  | Int   | 是        | 渠道类型, 1：奇点资讯， 2：黄历天气，3：纹字锁频，4：猎鹰浏览器，5：白牌  |
| ptype    | Int | 是         | 平台类型，1：IOS，2：安卓，3：网页，4：无法识别|
| aversion  | Stirng   | 是        | APP版本    |
| ctime  | Long   | 是        | 当前系统时间毫秒值    |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": 1   --广告来源:猎鹰广告api:1,广点通sdk:2 ,亦复广告api:3
  "feedAdPos":6 --非gdtsdk 来源则返回-1
  "relatedAdPos":4 --非gdtsdk 来源则返回-1
  "feedVideoAdPos": 4, --同上
  "relatedVideoAdPos": 1 --同上
}
```

### 13 热点

#### 13.1 爬虫上传热点新闻

_Request_

```json
POST /v2/hot/crawler/news
Content-Type:  application/x-www-form-urlencoded
Host: bdp.deeporiginalx.com
```
| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| news  | String[]   | 是        | 热点新闻集合


_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": "Upload Hot News Success"
}
```

#### 13.2 爬虫上传热词

_Request_

```json
POST /v2/hot/crawler/words
Content-Type:  application/x-www-form-urlencoded
Host: bdp.deeporiginalx.com
```
| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| words  | String[]   | 是        | 热词集合


_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": "Upload Hot words Success"
}
```

#### 13.3 前端获取热词

_Request_

```json
POST /v2/hot/words
Content-Type:  application/json
Host: bdp.deeporiginalx.com
```



_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
	"code": 2000,
    "data": [
    {
      "title": "十九大前夕习近平基层听民声问计于民"
    },
    {
      "title": "贵州选举党的十九大代表习近平全票当选"
    },
    {
      "title": "武汉房地产市场整治加码"
    },
	....
    ]
}
```

### 14 版本更新接口

#### 14.1 版本更新接口

_Request_

```json
GET /v2/version/query
Host: bdp.deeporiginalx.com
```
| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| uid  | Long   | 是        | 用户ID                                |
| ctype  | Int   | 是        | 渠道类型, 1：奇点资讯， 2：黄历天气，3：纹字锁频，4：猎鹰浏览器，5：白牌  |
| ptype    | Int | 是         | 平台类型，1：IOS，2：安卓，3：网页，4：无法识别|

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json
{
  "code": 2000,
  "data": {
    "ctype": 1,
    "ptype": 2,
    "version": "1.1.2",
    "version_code": 2,
    "updateLog": "更新日志",
    "downloadLink": "http://qidianapkstatic.oss-cn-beijing.aliyuncs.com/qidian_official_v3.6.3_20170303.apk",
    "forceUpdate": false,
    "md5": "a393cad9469c5da62454a115d0ef3f53"
  }
}
```