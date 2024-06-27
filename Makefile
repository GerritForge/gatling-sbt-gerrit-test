.PHONY:	run build prepare publish parallel-run

TARGET_SIMULATION?=GitSimulation
DOCKER_IMAGE:=gerritforge/gatling-sbt-gerrit-test
JOBS:=2

build: prepare
	sbt docker

prepare: id_rsa

id_rsa:
	ssh-keygen -t rsa -b 2048 -f ./id_rsa -N '' -m PEM

run:
	for simulation in GitSimulation GerritRestSimulation; do \
		docker run -e JAVA_OPTS="-Xmx4g" --rm --env-file simulation.env --env-file gerrit-simulation.env -v "$$(pwd)/target/gatling:/opt/gatling/results" \
			$(DOCKER_IMAGE) -s gerritforge.$$simulation --run-mode local; done
			
run-oauth:
	for simulation in GerritOauthSimulation; do \
		docker run -e JAVA_OPTS="-Xmx4g" --rm --env-file simulation.env --env-file gerrit-simulation.env -v "$$(pwd)/target/gatling:/opt/gatling/results" \
			$(DOCKER_IMAGE) -s gerritforge.$$simulation --run-mode local; done

run-single:
#Note that env variables with the same names will be clobbered
	docker run -e JAVA_OPTS="-Xmx4g" --rm --env-file simulation.env --env-file bitbucket-simulation.env --env-file gerrit-simulation.env -v "$$(pwd)/target/gatling:/opt/gatling/results" \
			$(DOCKER_IMAGE) -s gerritforge.$(TARGET_SIMULATION) --run-mode local

run-write-only:
#Note that env variables with the same names will be clobbered
	docker run -e JAVA_OPTS="-Xmx4g" --rm --env-file simulation.env --env-file gerrit-simulation.env -v "$$(pwd)/target/gatling:/opt/gatling/results" \
			$(DOCKER_IMAGE) -s gerritforge.GerritWriteOnlySimulation --run-mode local
background-job-%:
	mkdir -p `pwd`/target/gatling
	make run &> `pwd`/target/gatling/$@.log &

parallel-run:
	for i in $$(seq 1 ${JOBS}); do \
		make background-job-$$i; done

push: build
	sbt dockerBuildAndPush