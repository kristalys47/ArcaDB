FROM maven:latest
COPY . /Worker/
WORKDIR /Worker
RUN mvn clean install dependency:go-offline
CMD mvn exec:java -Dexec.mainClass="orc.main"
