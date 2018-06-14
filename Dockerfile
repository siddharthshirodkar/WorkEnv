FROM maven:3.3-jdk-8-onbuild 
FROM java:8
COPY --from=0 /usr/src/app/target/workenv-0.1.0.jar ~/workenv-0.1.0.jar
CMD ["java","-cp","workenv-0.1.0.jar","starter/start/ServiceGeneratorMain.java"]

#FROM java:8
#CMD ["javac","-cp",".","-d","bin","starter/start/ServiceGeneratorMain.java"]
