FROM java:8

CMD ["javac","-cp",".","-d","bin","starter/start/ServiceGeneratorMain.java"]
