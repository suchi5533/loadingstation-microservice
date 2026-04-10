FROM eclipse-temurin:17-jdk
COPY target/*.war app.war
ENTRYPOINT ["java","-jar","/app.war"]