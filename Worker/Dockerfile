FROM maven:latest
COPY ./Worker/ /Worker/
WORKDIR /Worker
RUN mvn install
CMD mvn exec:java -Dexec.mainClass="orc.main"