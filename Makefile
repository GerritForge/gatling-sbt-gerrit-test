.PHONY:	run build prepare publish parallel-run

DOCKER_IMAGE:=gerritforge/gatling-sbt-gerrit-test
JOBS:=2

build: prepare
	sbt docker

prepare: id_ed25519

id_ed25519:
	ssh-keygen -t ed25519 -b 2048 -f ./id_ed25519 -N '' -m PEM

run:
	for simulation in GerritGitSimulation GerritRestSimulation; do \
		docker run -e JAVA_OPTS="-Xmx4g" --rm --env-file simulation.env -v "$$(pwd)/target/gatling:/opt/gatling/results" \
			$(DOCKER_IMAGE) -s gerritforge.$$simulation --run-mode local; done

background-job-%:
	mkdir -p `pwd`/target/gatling
	make run &> `pwd`/target/gatling/$@.log &

parallel-run:
	for i in $$(seq 1 ${JOBS}); do \
		make background-job-$$i; done

push: build
	sbt dockerBuildAndPush
