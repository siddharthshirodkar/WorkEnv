FROM maven:3.3-jdk-8-onbuild

CMD ["javac","-cp",".","starter/start/ServiceGeneratorMain.java"]
