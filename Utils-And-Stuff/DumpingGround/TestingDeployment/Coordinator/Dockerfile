FROM maven:latest
COPY . /Distributed-DBMS/
WORKDIR /Distributed-DBMS
RUN apt-get update && apt-get install telnet
RUN IP=$(hostname -i)
RUN mvn clean install
CMD mvn exec:java -Dexec.mainClass="coordinator.Coordinator" -Dexec.args=$IP
