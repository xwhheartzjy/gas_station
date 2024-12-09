# 使用 OpenJDK 8 作为基础镜像
FROM openjdk:8-jdk

# 维护者信息
LABEL maintainer="xuwenhui553@163.com"

# 将本地的 jar 文件添加到容器内
COPY target/gas_station-1.0-SNAPSHOT.jar app.jar
#COPY src/main/resources/keystore.jks /app/keystore.jks

# 设置容器启动时的命令
ENTRYPOINT ["java", "-jar", "/app.jar"]

# 暴露应用运行的端口（如需要）
EXPOSE 9090
