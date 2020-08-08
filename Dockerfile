FROM denvazh/gatling:3.2.1

ENV MAVEN=https://repo1.maven.org/maven2

ADD https://gerrit-ci.gerritforge.com/job/gatling-git-sbt-master/lastSuccessfulBuild/artifact/target/scala-2.12/gatling-git-extension.jar /opt/gatling/lib/
ADD $MAVEN/commons-io/commons-io/2.6/commons-io-2.6.jar /opt/gatling/lib/
ADD target/scala-2.12/gatling-sbt-gerrit-test-assembly-0.1.0-SNAPSHOT.jar /opt/gatling/lib/

COPY ./src/test/scala/gerritforge /opt/gatling/user-files/simulations
COPY ./src/test/resources/logback.xml /opt/gatling/conf
COPY ./src/test/resources/application.conf /opt/gatling/conf

COPY id_rsa /root/.ssh/
COPY id_rsa.pub /root/.ssh/
COPY ssh_config /root/.ssh/config

RUN mkdir -p /opt/gatling/results
