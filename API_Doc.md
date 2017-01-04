# 数据平台接口文档_V3.4

## 目录


[TOC]

----
## 更新日志
*V3.4:*

1. 新闻详情页: 添加参数s， 详情页图片地址是否http改https
2. 频道刷新加上广告: 添加参数s， feed流图片地址是否http改https;  添加参数v，是否显示视频
3. 频道加载加上广告: 添加参数s， feed流图片地址是否http改https;  添加参数v，是否显示视频
4. 添加接口:
	视频频道(刷新)， 视频频道(加载)， 视频详情页

*V3.3:*

新增接口：

1. 转发记录
2. 不感兴趣记录

修改接口:

1. 搜索接口/v2/ns/es/s,添加uid参数,方便记录用户搜索记录


*V3.2:*
新增：

1. 用户手机信息,手机app列表收集接口

*V3.1:*

客户端（新增）：

1. 专题详情 接口
2. 奇点频道 新增专题内容

*V3.0:*

客户端（新增）：

1. 本地用户 注册、登陆、修改密码、重置密码 4个接口

*V2.9:*

客户端（新增）：

1. 频道刷新加上广告
2. 频道加载加上广告

*V2.8.1:*

客户端(修改)：

1. 用户评论删除：原有接口调用参数为(uid, did)，修正为(cid, did)，即评论 ID、评论 docid；
2. 用户查看 **个人** 评论列表接口：新增`nid`、`ntitle`字段，对应“新闻ID”、“新闻标题”。

*V2.8:*

客户端（新增）：

1. 起点频道刷新：大图、推送、热点、普通新闻混合显示
2. 起点频道加载：同上


*V2.7:*

客户端（修改）：

1. 频道列表接口：新增请求参数`sech`，是(1)否(0)同时获取对应频道的二级频道列表
2. 新闻频道列表（“按频道”列表页刷新、加载，除“频道 ID 为 1 之外的频道”），如果同时提供二级频道 ID，将会按该二级获取新闻列表
3. 所有新闻列表页(feed)返回的数据结构，新增`tags`字段，即“关键字”

爬虫（修改）：

1. 新闻缓存提交，数据格式增加`se_channel_id`字段，即该新闻对应的二级频道 ID，来自`sourcelist_v2`表，数字类型，可为空，不能为空字符串("")

*V2.6.1:*

客户端（新增）：

1. 指定新闻发布源的新闻列表（翻页加载，无需登录）

*V2.6:*

1. 客户端（新增）
   1. 用户添加新闻发布源
   2. 用户删除新闻发布源
   3. 查看用户关注源列表
   4. 用户已关注发布源相关的新闻列表刷新(作为两个单独的接口，不与原有列表接口混用)
   5. 用户已关注发布源相关的新闻列表加载
2. 客户端（修改）
   1. 详情页接口响应数据，新增字段(需要提供 UID，可选，即当用户为正式注册用户时提供 UID 以获得如下数据)：colflag(是否已收藏该新闻)、conflag(是否已关心该新闻)、conpubflag(是否已关心该新闻的发布源)
3. 爬虫
   1. 新闻缓存提交部分，新增字段：pub_icon(发布源图表)、pub_descr(发布源描述)

_V2.5：_

1. 列表页响应增加 `descr` (摘要)字段

_V2.4：_
1. 新增图片服务说明

_V2.3：_
1. 目录 **客户端接口-新闻列表页** 中的 6 种列表页接口新增`tmk`参数，是(1)否(0)将部分新闻的发布时间(ptime)改为 5 分钟以内
2. 列表页与详情页响应数据中移除`url`字段

_V2.2：_
1. 新增爬虫新闻提交接口

_V2.1：_
1. 错误修改
2. 新增客户端日志上报接口
3. 新增爬虫新闻上报接口

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

### 用户

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

----
#### 游客用户注册

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
#### 游客用户登录

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
#### 三方用户注册

_Request_

```json
POST /v2/au/sin/s
Host: bdp.deeporiginalx.com
Content-Type: application/json

{
  "muid": 4,                          - Option - 游客合并三方时提供该字段
  "msuid": "weibo_suid_xxx",          - Option - 三方之间进行合并时提供该字段
  "utype": 3,
  "platform": 1,
  "suid": "weibo_suid_xxx1",
  "stoken": "weibo_stoken_xxx",
  "sexpires": "2016-4-27 17:37:22",
  "uname": "zhange",                  - Option
  "gender": 1,                        - Option
  "avatar": "http://tva4.jpg",        - Option
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
#### 本地用户注册

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
#### 本地用户登录

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
#### 本地用户修改密码

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
#### 本地用户重置密码

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

### 新闻频道

----
#### 普通频道

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
#### 用户定制频道

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
#### 用户定制频道修改

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
### 新闻源列表

_Request_

```json
GET /v2/ns/srcs
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须     | 参数解释 |
| ---- | :----- | :------- | :--- |
| p    | String | 否(默认 1)  | 页数   |
| c    | String | 否(默认 20) | 条数   |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": [
    {
      "id": 7,
      "sname": "今日头条-财经",
      "descr": "今日头条财经新闻频道",
      "status": 1,
      "cname": "财经",
      "cid": 7,
      "state": 1
    },
    ...
  ]
}
```

----
### 新闻列表页

**列表页响应统一格式**

```json
HTTP/1.1 200 OK
Content-Type: application/json
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA

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
      "descr": "俄天然气外交走向终结...",                        - 新闻内容摘要
      "tags": ["欧洲", "多元化"]								  - 关键字
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
    ...
  ]
}
```

----
#### 列表页刷新

_Request_

```json
GET /v2/ns/fed/r
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
| scid | String | 否        | 二级频道 ID，该二级频(scid)道必须是一级频道(cid)的子频道 |

----

#### 列表页加载

_Request_

```json
GET /v2/ns/fed/l
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
----

#### 行政区划-列表页刷新

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
#### 行政区划-列表页加载

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
#### 新闻源-列表页刷新

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
#### 新闻源-列表页加载

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
### 新闻详情页

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
### 新闻相关搜索列表

_Request_

```json
GET /v2/ns/asc
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须     | 参数解释 |
| ---- | :----- | :------- | :--- |
| nid  | String | 是        | 新闻ID |
| p    | String | 否(默认 1)  | 页数   |
| c    | String | 否(默认 20) | 条数   |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": [
    {
      "url": "http://news.163.com/16/0520/08/BNGEG7ID00014Q4P.html",
      "title": "但愿雷洋事件不是一个小插曲",
      "from": "Baidu",
      "rank": 1,
      "pname": "网易新闻",
      "ptime": "2016-05-20 08:47:45",
      "img": "http://some.jpg",         - Option
      "abs": "据@平安北京19日发布..."   - Option
    },
    ...
  ]
}
```

----
### 新闻普通评论列表

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
### 新闻热点评论列表

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

### 新闻-用户相关操作

#### 新闻评论

----
##### 创建评论

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
##### 删除评论

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
##### 查看评论列表

_Request_

```json
GET /v2/ns/au/coms
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须 | 参数解释 |
| ---- | :----- | :--- | :--- |
| uid  | String | 是    | 用户ID |

#### 新闻评论点赞

----
##### 点赞

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
##### 取消赞

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

#### 新闻收藏

----
##### 添加收藏

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
##### 取消收藏

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
##### 查看收藏列表

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

#### 新闻关心

----
##### 添加关心

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
##### 取消关心

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
##### 查看关心列表

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

**Response：新闻列表页数据格式**

----
#### 新闻发布源关心

----

##### 添加新闻发布源关心

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

##### 取消新闻发布源关心

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

##### 已关心发布源列表

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

##### 已关心发布源的新闻列表刷新

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

##### 已关心发布源的新闻列表加载

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

##### 指定新闻发布源新闻列表

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

### 日志上报

----
#### 点击日志上报

_Request_

```json
GET /rep/v2/c
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须 | 参数解释 |
| ---- | :----- | :--- | :--- |
| u    | String | 是    | 用户ID |
| p    | String | 否    | 省份   |
| t    | String | 否    | 市    |
| i    | String | 否    | 地区/县 |
| d    | String | 是    | 数据   |

字段 d 的数据格式初始为JSON，通过base64加密并移除末尾空格：

```json
"d":[
  {
    "n": 234,   - 新闻ID
    "c": 12,    - 频道ID
    "t": 2,     - 新闻类型ID
    "s": 23,    - 停留时长，秒
    "f": 1      - 进入位置ID
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
#### 手机信息日志上报

_Request_

```json
GET /rep/v2/p
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须 | 参数解释 |
| ---- | :----- | :--- | :--- |
| u    | String | 是    | 用户ID |
| p    | String | 否    | 省份   |
| t    | String | 否    | 市    |
| i    | String | 否    | 地区/县 |
| d    | String | 是    | 数据   |

字段 d 的数据格式初始为JSON，通过base64加密并移除末尾空格：

```json
"d":{
    "b": "苹果",   		- 品牌
    "v": "iPhone7",    - 型号
    "apps": [
        {
            "ai": "1",          - app_id
            "an": "奇点资讯",    - app_name
            "ac": 1             - is_active是否活跃(1：活跃，0：不活跃)
        }
        ...
    ]
  }

```

1. 将字段d的值(不包括字段名[“d”:]部分)进行base64加密 - ImQiOlt7Im4iOiAyMzQsImMiOiAxMiwidCI6IDIsInMiOiAyMywiZiI6IDF9XQ==
2. 移除空格 - ImQiOlt7Im4iOiAyMzQsImMiOiAxMiwidCI6IDIsInMiOiAyMywiZiI6IDF9XQ
3. 与其他参数一起进行URLEncode，构建请求出URL

**示例**

```sh
GET http://bdp.deeporiginalx.com/rep/v2/p?u=22&p=%E5%8C%97%E4%BA%AC&t=%E5%8C%97%E4%BA%AC&i=%E4%B8%9C%E5%9F%8E&d=ImQiOlt7Im4iOiAyMzQsImMiOiAxMiwidCI6IDIsInMiOiAyMywiZiI6IDF9XQ

HTTP/1.1 200 OK
Content-Type: image/gif
```


## 内部服务接口

### 爬虫信息管理

----
#### 查看爬虫队列列表

_Request_

```json
GET /v2/sps/qs
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须     | 参数解释 |
| ---- | :----- | :------- | :--- |
| p    | String | 否(默认 1)  | 页数   |
| c    | String | 否(默认 20) | 条数   |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": [
    {
      "queue": "list_spider_JRTT_Baby:start_urls",
      "spider": "JRTT_Baby"
    },
    ...
  ]
}
```

----
#### 创建爬虫队列

_Request_

```json
POST /v2/sps/qs
Content-Type: application/json
Host: bdp.deeporiginalx.com

{
  "queue": "queue",
  "spider": "spider",
  "descr": "descr"
}
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": "queue"
}
```

----
#### 更新爬虫队列

_Request_

```json
PUT /v2/sps/qs
Content-Type: application/json
Host: bdp.deeporiginalx.com

{
  "queue": "queue",
  "spider": "spider1",
  "descr": "descr1"
}
```

| Key  | 参数类型   | 是否必须 | 参数解释     |
| ---- | :----- | :--- | :------- |
| q    | String | 是    | 队列名queue |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": {
    "queue": "queue",
    "spider": "spider1",
    "descr": "descr1"
  }
}
```

----
#### 删除爬虫队列

_Request_

```json
DELETE /v2/sps/qs
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须 | 参数解释     |
| ---- | :----- | :--- | :------- |
| q    | String | 是    | 队列名queue |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": "queue"
}
```

----
#### 查看爬虫抓取源列表

_Request_

```json
GET /v2/sps/srcs
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须     | 参数解释     |
| ---- | :----- | :------- | :------- |
| sa   | String | 是        | 是否上线，0或1 |
| su   | String | 是        | 是否调度，0或1 |
| p    | String | 否(默认 1)  | 页数       |
| c    | String | 否(默认 20) | 条数       |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": [
    {
      "id": 7,
      "ctime": "2015-11-19 00:00:00",
      "surl": "http://toutiao.com/api/article/recent/?source=2&count=20&category=news_finance&utm_source=toutiao&offset=0",
      "sname": "今日头条-财经",
      "descr": "今日头条财经新闻频道",
      "queue": "spider:news:toutiao:start_urls",
      "rate": 3,
      "status": 1,
      "cname": "财经",
      "cid": 7,
      "pconf": {},
      "state": 0
    },
    ...
  ]
}
```

----
#### 查看爬虫抓取源列表-队列

_Request_

```json
GET /v2/sps/srcs/q
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须     | 参数解释      |
| ---- | :----- | :------- | :-------- |
| q    | String | 是        | 队列名 queue |
| p    | String | 否(默认 1)  | 页数        |
| c    | String | 否(默认 20) | 条数        |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": [
    {
      "id": 793,
      "ctime": "2016-04-28 17:14:46",
      "sname": "Knowing新闻",
      "queue": "spider:news:app:start_urls",
      "rate": 20,
      "status": 0,
      "cname": "APP",
      "cid": 35,
      "state": 1
    }
  ]
}
```

----
#### 创建爬虫抓取源

_Request_

```json
POST /v2/sps/srcs HTTP/1.1
Content-Type: application/json
Host: bdp.deeporiginalx.com

{
  "ctime": "2016-05-22 21:41:05",
  "surl": "surl",
  "sname": "sname",
  "descr": "descr",
  "queue": "queue",
  "rate": 5,
  "status": 1,
  "cname": "体育",
  "cid": 5,
  "state": 1
}
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": 2
}
```

----
#### 更新爬虫抓取源

_Request_

```json
PUT /v2/sps/srcs
Content-Type: application/json
Host: bdp.deeporiginalx.com

{
  "ctime": "2016-05-22 21:41:05",
  "surl": "surl",
  "sname": "sname111",
  "descr": "descr",
  "queue": "queue112",
  "rate": 5,
  "status": 1,
  "cname": "体育",
  "cid": 35,
  "state": 1
}
```

| Key  | 参数类型   | 是否必须 | 参数解释     |
| ---- | :----- | :--- | :------- |
| q    | String | 是    | 队列名queue |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": 1
}
```

----
#### 删除爬虫抓取源

_Request_

```json
DELETE /v2/sps/srcs
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须 | 参数解释  |
| ---- | :----- | :--- | :---- |
| sid  | String | 是    | 抓取源ID |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": 1
}
```

----
### 爬虫抓取源调度

**队列内容格式：**

```json
{
  "sid": 1,
  "surl": "http://bdp.com",
  "meta": {
    "cid": 2,
    "sname": "Testing",
    "state": 1,
    "pconf": {
      "key":"value"
    }
  }
}
```

----
#### 开启所有抓取源

_Request_

```json
POST /v2/sps/dp/start/all
Host: bdp.deeporiginalx.com
```

----
#### 重载所有抓取源

_Request_

```json
POST /v2/sps/dp/reload/all
Host: bdp.deeporiginalx.com
```

----
#### 关闭所有抓取源

_Request_

```json
POST /v2/sps/dp/close/all
Host: bdp.deeporiginalx.com
```

----
#### 开启指定抓取源

_Request_

```json
POST /v2/sps/dp/start/{sid}
Host: bdp.deeporiginalx.com
```

----
#### 重载指定抓取源

_Request_

```json
POST /v2/sps/dp/reload/{sid}
Host: bdp.deeporiginalx.com
```

----
#### 关闭指定抓取源

_Request_

```json
POST /v2/sps/dp/close/{sid}
Host: bdp.deeporiginalx.com
```

----
#### PUSH指定抓取源

_Request_

```json
POST /v2/sps/dp/push/{sid}
Host: bdp.deeporiginalx.com
```

### 爬虫数据提交

----
#### 新闻数据提交

_Request_

```json
POST /v2/sps/ns/{base64.encode(task_id)}
Host: bdp.deeporiginalx.com

"task_key":{                                                 
  "url": "http://j.news.163.com/docs.html",                  - 抓取URL:String NOT (NULL & "")
  "title": "移民精神感动北京",                               - 标题:String NOT (NULL & "")
  "keywords": "北京,移民",                                   - 关键字,使用英文逗号“,”隔开:String NULL NOT ""
  "author": "编辑:作者",                                     - 作者:String NULL NOT ""
  "pub_time": "2016-04-15 12:57:00",                         - 发布时间,格式为"yyyy-MM-dd HH:mm:ss":String NULL NOT ""
  "pub_name": "新华网",                                      - 发布媒体名,String NULL NOT ""
  "pub_url": "http://www.xinhua.com/asads.html",             - 发布URL:String NULL
  "pub_icon": "http://some.png",							- 发布源图标:String NULL NOT ""
  "pub_descr": "新华网新闻...",								- 发布源描述:String NULL NOT ""
  "content_html": "<html>...</html>",                        - 抓取网页源码:String NOT (NULL & "")
  "synopsis": "简介",                                        - 简介:String NULL NOT ""
  "province": "北京市",                                      - 省:String NULL NOT ""
  "city": "北京市",                                          - 市:String NULL NOT ""
  "district": "朝阳区",                                      - 区/县:String NULL NOT ""
  "docid": "asddaa",                                         - 与评论对应的docid:String NOT (NULL & "")
  "content": [                                               - 按如下格式的Json数据转为String: String NOT NULL
      {"img": "http://easyread.ph.126.net/lY5eDj_TE0XMPe-gAl1yQA==/7917060963595944021.jpg"},
      {"txt": "\u6dc5\u5ddd\u4eba\u6c11\u6cea\u522b\u5bb6\u56ed\uff08\u4f59\u7acb\u65b0\u6444\uff09"}
    ],                                                       - 现在支持的格式有：txt，img，vid，不能是其他键
  "channel_id": "12",                                        - 频道ID:String NOT (NULL & "")
  "se_channel_id": "23"										 - 二级频道 ID，String  NULL NOT ""
  "source_id": "23",                                         - 抓取源ID:String NOT (NULL & "")
  "source_online": "1",                                      - 抓取源上线标志位:String NOT (NULL & "")
  "task_conf": {                                             - 数据处理配置，Json转字符串:String NULL  NOT ""
    "xxxx": "1",
    "xxxx": "1"
  },
  
  "comment_queue":"xxxx:start_urls",                         - 评论抓取任务的队列名：String NULL
  "comment_task": "Json_to_String"                           - 评论抓取任务，由Json转换成的String：String NULL
}
```

_Response_

```json
HTTP/1.1 200 OK

{
  "code": 2000,
  "data": "news:aHR0cDovL2oubmV3cy4xNjMuY29tL2RvY3MvMi8yMDE2MDUyOTExL0JPOFFLRlAxOTAwMUtGUDIuaHRtbA"
}
```

----
#### 评论数据提交

_Request_

```json
POST /v2/sps/coms
Content-Type: application/json
Host: bdp.deeporiginalx.com

{
  "content": "66666",                     -正文，不可为空字符串 ""
  "commend": 0,                           - 赞数，初始设置为 0
  "ctime": "2016-05-24 19:22:11",
  "uname": "zhange",
  "avatar": "http://customtouxiang.jpg"   - Option
  "docid": "http://toutiao.com/group/6287267555786981633/comments/?count=100&offset=0&format=json",
  "cid": "comment_id_of_weibo",           - Option - 抓取时获取的源网站评论ID，(cid, docid)为联合唯一键
  "pid" "parent_comment_id_of_weibo"      - Option - 关联的父级评论 cid
}
```

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": 8                                 - 插入的评论ID
}
```

----
### <span id="新闻搜索">新闻搜索</span>


#### <span id="搜索">搜索</span>
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

#### <span id="搜索新闻及订阅号">搜索新闻及订阅号</span>
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

----
## <span id="新闻推荐">新闻推荐</span>


### <span id="人工新闻推荐">人工新闻推荐</span>

#### <span id="推荐/取消推荐CMS">推荐/取消推荐 新闻CMS</span>
_Request_

```json
GET /v2/nsr/o
Content-Type: application/json
Host: bdp.deeporiginalx.com
```
| Key  | 参数类型   | 是否必须 | 参数解释                     |
| ---- | :----- | :--- | :----------------------- |
| nid  | Long   | 是    | 新闻id                     |
| m    | String | 是    | 方法名称:添加删除(insert/delete) |
| l    | Double | 是    | 推荐等级                     |
| b    | Int    | 否    | 大图为第几张                   |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": Long
}
```

#### <span id="展示推荐新闻列表CMS">展示推荐新闻列表CMS</span>
_Request_

```json
GET /v2/nsr/l
Content-Type: application/json
Host: bdp.deeporiginalx.com
```
| Key  | 参数类型 | 是否必须     | 参数解释    |
| ---- | :--- | :------- | :------ |
| ch   | Long | 否        | 频道      |
| ifr  | Int  | 是(1/0)   | 已推荐/未推荐 |
| p    | Long | 否(默认 1)  | 页数      |
| c    | Long | 否(默认 20) | 条数      |

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
      "district": "山东",
      "rtime":  "2016-05-22 01:20:46",                        - 推荐时间
      "level":  2,                                            - 推荐等级
      "bigimg": 1                                             - 第几张为大图
      "status": 1                                             - 推荐状态1为已推荐
    },
    …
  ]
  "total": 2000,                                              - 总条数
}
```


#### <span id="搜索新闻CMS">搜索新闻CMS</span>
_Request_

```json
GET /v2/nsr/es/l
Content-Type: application/json
Host: bdp.deeporiginalx.com
```
| Key      | 参数类型   | 是否必须     | 参数解释  |
| -------- | :----- | :------- | :---- |
| keywords | String | 是        | 搜索关键字 |
| pn       | String | 否        | 新闻来源  |
| ch       | Long   | 否        | 频道    |
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
      "district": "山东",
      "rtime":  "2016-05-22 01:20:46",                        - 推荐时间
      "level":  2,                                            - 推荐等级
      "bigimg": 1                                             - 第几张为大图
      "status": 1                                             - 推荐状态1为已推荐
    },
    …
  ]
  "total": 2000,                                              - 总条数
}
```
----
#### 起点频道刷新

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
      "style": 1,                                             - 列表图格式，0、1、2、3, 11:第一张为大图、12:第二张为大图、13:第三张为大图
      "imgs": [                                               - 该字段会有对应style数值的图片
        "http://bdp-pic.deeporiginalx.com/W0JAMjM4Mjc4ZDE.png"
      ],
      "province": "山东",
      "city": "青岛",
      "district": "山东",
      "rtype": "0"                                              - 推荐类型:0普通、1热点、2推送、3广告、4专题
    },
    …
  ]
}
```
----
#### 奇点频道加载

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
#### 频道刷新加上广告

_Request_

```json
POST /v2/ns/fed/ra
Host: bdp.deeporiginalx.com
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
```

| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| cid  | Long | 是        | 频道ID                                |
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
      "style": 1,                                             - 列表图格式，0、1、2、3, 11:第一张为大图、12:第二张为大图、13:第三张为大图
      "imgs": [                                               - 该字段会有对应style数值的图片
        "http://bdp-pic.deeporiginalx.com/W0JAMjM4Mjc4ZDE.png"
      ],
      "province": "山东",
      "city": "青岛",
      "district": "山东",
      "rtype": "3"                                              - 推荐类型:0普通、1热点、2推送、3广告、4专题、5图片新闻、6视频
    },
    …
  ]
}
```
----
#### 频道加载加上广告

_Request_

```json
POST /v2/ns/fed/la
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
Host: bdp.deeporiginalx.com
```

| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| cid  | Long | 是        | 频道ID                                |
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
#### 专题详情

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
            ]
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
            "city": "上海"
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
#### 用户手机信息,手机app列表收集接口

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
##### 转发记录

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
##### 不感兴趣记录

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


----
#### 视频频道(刷新)

_Request_

```json
POST /v2/ns/fed/ra
Host: bdp.deeporiginalx.com
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
```

| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| cid  | Long | 是        | 频道ID:42                                |
| tcr  | Long | 是        | 起始时间，13位时间戳                         |
| tmk  | Int | 否(默认 1)  | 是(1)否(0)模拟实时发布时间(部分新闻的发布时间修改为5分钟以内) |
| p    | Long | 否(默认 1)  | 页数                                  |
| c    | Long | 否(默认 20) | 条数                                  |
| uid  | Long   | 是        | 用户ID                                |
| b    | String(base64编码) | 是         | 广告调用传的规格参数,具体见广告调用pdf,用base64编码处理|
| t    | Int    | 否(默认 0)        | 显示专题  是(1)否(0)                              |
| s    | Int    | 否(默认 0)        | 显示https图片地址  是(1)否(0)                      |
| v    | Int    | 否(默认 0)        | 显示视频  是(1)否(0)                              |
| nid  | Long | 否        | 最大视频ID                                |
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
      "style": 1,                                             - 视频格式，6、7、8
      "imgs": [                                               - 该字段会有对应style数值的图片
        "http://bdp-pic.deeporiginalx.com/W0JAMjM4Mjc4ZDE.png"
      ],
      "province": "山东",
      "city": "青岛",
      "district": "山东",
      "videourl": "http://gslb.miaopai.com/stream/sHLX8Z6pr0hAKyK6SDucXA__.mp4?yx=&refer=weibo_app&Expires=1482375610&ssig=MtRxNEvf0N&KID=unistore,video", -视频url
      "thumbnail": "http://bdp-pic.deeporiginalx.com/W0JAMjIzZmZhZGQ.jpg", -背景图
      "rtype": "6"                                              - 推荐类型:0普通、1热点、2推送、3广告、4专题、5图片新闻、6视频
    },
    …
  ]
}
```
----
#### 视频频道(加载)

_Request_

```json
POST /v2/ns/fed/la
Host: bdp.deeporiginalx.com
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
```

| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| cid  | Long | 是        | 频道ID:42                                |
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
      "style": 1,                                             - 视频格式，6、7、8
      "imgs": [                                               - 该字段会有对应style数值的图片
        "http://bdp-pic.deeporiginalx.com/W0JAMjM4Mjc4ZDE.png"
      ],
      "province": "山东",
      "city": "青岛",
      "district": "山东",
      "videourl": "http://gslb.miaopai.com/stream/sHLX8Z6pr0hAKyK6SDucXA__.mp4?yx=&refer=weibo_app&Expires=1482375610&ssig=MtRxNEvf0N&KID=unistore,video", -视频url
      "thumbnail": "http://bdp-pic.deeporiginalx.com/W0JAMjIzZmZhZGQ.jpg", -背景图
      "rtype": "6"                                              - 推荐类型:0普通、1热点、2推送、3广告、4专题、5图片新闻、6视频
    },
    …
  ]
}
```
----
### 视频详情页

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