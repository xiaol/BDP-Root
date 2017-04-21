# 部署流程

## 服务器

- `121.40.176.109`
- `121.41.82.132`
- `121.41.51.80`

## 部署方式

部署路径在`/data/pro`，使用`supervisor`方式部署。

1. 本地打包应用：

   1. `cd /PROJECT_HOME/BDP_Root`
   2. 终端中：`sbt`
   3. 打包：`webserver/compile` & `webserver/dist`

2. 将打包后的应用上传至服务器：`scp web-server-0.0.1.zip ...`

3. 服务器停止服务并备份：

   1. `cd /data/pro`
   2. `supervisorctl stop api`
   3. `rm -fr web-server-0.0.1` & `mv web-server-0.0.1.zip backends/`

4. 解包新版本应用，新版压缩包必须在`/data/pro`目录下
    unzip web-server-0.0.1.zip

5. 替换配置文件
    `cp /data/pro/application.conf  web-server-0.0.1/conf/`

5. 启动服务：`supervisorctl start api`

6. 查看服务状态：`supervisorctl status`

    

