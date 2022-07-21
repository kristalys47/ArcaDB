FROM maven:latest
COPY . /Distributed-DBMS/
WORKDIR /Distributed-DBMS
RUN mvn clean install
CMD mvn exec:java -Dexec.mainClass="orc.main"
