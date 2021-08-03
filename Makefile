.PHONY:	run build prepare publish parallel-run

DOCKER_IMAGE:=gerritforge/gatling-sbt-gerrit-test
JOBS:=2

build: prepare
	sbt docker

prepare: id_rsa

id_rsa:
	ssh-keygen -t rsa -b 2048 -f ./id_rsa -N '' -m PEM

run:
	for simulation in GerritGitSimulation GerritRestSimulation; do \
		docker run --rm --env-file simulation.env -v "$$(pwd)/target/gatling:/opt/gatling/results" \
			$(DOCKER_IMAGE) -s gerritforge.$$simulation; done

background-job-%:
	mkdir -p `pwd`/target/gatling
	make run &> `pwd`/target/gatling/$@.log &

parallel-run:
	for (( i=0; i<$(JOBS); i++)); do \
		make background-job-$$i; done

push: build
	sbt dockerBuildAndPush