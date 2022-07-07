FROM maven:latest
COPY . /Distributed-DBMS/
WORKDIR /Distributed-DBMS
RUN apt-get update && apt-get install telnet
RUN IP=$(hostname -i)
RUN mvn clean install
RUN chmod +x ./wait-for-it.sh
CMD mvn exec:java -Dexec.mainClass="orc.main" -Dexec.args=$IP

