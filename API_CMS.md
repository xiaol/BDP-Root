# CMS接口文档_V3.10

## 目录
[TOC]

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


----
## 新闻推荐

----
### 人工新闻推荐
----
#### 推荐/取消推荐
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
----
#### 展示推荐新闻列表
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
      "bigimg": 1,                                            - 第几张为大图
      "status": 1,                                            - 推荐状态1为已推荐
      "showcount": 1,                                         - 展示次数
      "clickcount": 1                                         - 点击次数
    },
    …
  ]
  "total": 2000,                                              - 总条数
}
```

----
#### 搜索新闻
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
#### pvuv统计

_Request_

```json
GET /v2/pvuv/q
Host: bdp.deeporiginalx.com
Authorization: Basic X29pZH5jeDYyMmNvKXhuNzU2NmVuMXNzJy5yaXg0aWphZWUpaTc0M2JjbG40M2l1NDZlYXE3MXcyYV94KDBwNA
```

| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| p    | Long | 否(默认 1)  | 页数                                  |
| c    | Long | 否(默认 30) | 条数                                  |

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": [
    {
          "id": 790,
          "pv": 335199,                                 --总pv (说明:总pv > 安卓pv + ios pv, 因为有些操作没有用户id,无法判断是什么类型用户)
          "data_time_count": "2017-01-09 00:00:00",     --统计时间(统计前一天数据)
          "androidpv": 162370,                          --安卓pv
          "iospv": 36659,                               --ios pv
          "androiduv": 27106,                           --安卓uv
          "iosuv": 23115                                --ios uv  
                                                        --(总uv = 安卓uv + ios uv)
        },
    …
  ]
}
```
----
### 统计报表
----
#### 每日点击量前100

_Request_

```json
GET /v2/re/top
Host: bdp.deeporiginalx.com
```
| Key  | 参数类型   | 是否必须     | 参数解释                                |
| ---- | :----- | :------- | :---------------------------------- |
| ctype  | Int   | 是        | 渠道类型, 1：奇点资讯， 2：黄历天气，3：纹字锁频，4：猎鹰浏览器，5：白牌  |
| ptype  | Int | 是         | 平台类型，1：IOS，2：安卓，3：网页，4：无法识别|
| page   | Int | 否         | 页数|
| count  | Int | 否         | 每页条数|

_Response_

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "code": 2000,
  "data": [
    {
      "nid": 12374346,
      "title": "天呐！陈伟霆一直声称不上跑男，baby一走他就来了",
      "clickcount": 47,                 -- 点击数
      "showcount": 14224,               -- 展示数
      "ctype": 3,                       -- 渠道
      "ptype": 2,                       -- 平台
      "data_time_count": "2017-02-23 00:00:00" --数据时间
    },
    {
      "nid": 12382712,
      "title": "图集：8张图告诉你空姐真正的私人生活，原来和我们见到的差别极大",
      "clickcount": 37,
      "showcount": 13790,
      "ctype": 3,
      "ptype": 2,
      "data_time_count": "2017-02-23 00:00:00"
    },
    ......
  ]
}
```