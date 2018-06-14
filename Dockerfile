FROM maven:3.3-jdk-8-onbuild 
FROM java:8
COPY --from=0 /usr/src/app/target/workenv-0.1.0.jar ~/workenv-0.1.0.jar
CMD ["java","-jar","workenv-0.1.0.jar"]

#FROM java:8
#CMD ["javac","-cp",".","-d","bin","starter/start/ServiceGeneratorMain.java"]
