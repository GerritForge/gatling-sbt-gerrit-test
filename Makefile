.PHONY:	run build prepare publish

DOCKER_IMAGE:=gerritforge/gatling-sbt-gerrit-test
NUM_CONTAINERS:=1

build: prepare
	sbt docker

prepare: id_rsa

id_rsa:
	ssh-keygen -t rsa -b 2048 -f ./id_rsa -N '' -m PEM

run:
	for (( i=0; i<$(NUM_CONTAINERS); i++)); do \
		for simulation in GerritGitSimulation GerritRestSimulation; do \
			docker run -d --rm -ti --env-file simulation.env -v `pwd`/target/gatling:/opt/gatling/results \
				$(DOCKER_IMAGE) -s gerritforge.$$simulation; done; done

push: build
	sbt dockerBuildAndPush