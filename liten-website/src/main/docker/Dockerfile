FROM java:8
ADD liten-${project.version}-jar-with-dependencies.jar liten.jar
EXPOSE 50051 50505
ENTRYPOINT ["java", "-jar", "liten.jar", "-Dcamino=true"]