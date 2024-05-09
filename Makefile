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

run-single:
#We're passing both bitbucket and gerrit simulation regardless of which simulation we're running,
#to keep the make command simple, ideally we can optimise this going forward.
	docker run -e JAVA_OPTS="-Xmx4g" --rm --env-file simulation.env --env-file bitbucket-simulation.env --env-file gerrit-simulation.env -v "$$(pwd)/target/gatling:/opt/gatling/results" \
			$(DOCKER_IMAGE) -s gerritforge.$(TARGET_SIMULATION) --run-mode local

background-job-%:
	mkdir -p `pwd`/target/gatling
	make run &> `pwd`/target/gatling/$@.log &

parallel-run:
	for i in $$(seq 1 ${JOBS}); do \
		make background-job-$$i; done

push: build
	sbt dockerBuildAndPush