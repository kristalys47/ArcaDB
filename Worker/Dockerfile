FROM maven:latest
COPY . /Worker/
WORKDIR /Worker
RUN mvn clean install
CMD mvn exec:java -Dexec.mainClass="orc.main"
