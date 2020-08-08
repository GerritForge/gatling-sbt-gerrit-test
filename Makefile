.PHONY:	run build prepare publish

DOCKER_IMAGE:=gerritforge/gatling-sbt-gerrit-test

build: prepare
	sbt assembly
	docker build -t $(DOCKER_IMAGE) .

prepare: id_rsa

id_rsa:
	ssh-keygen -t rsa -b 2048 -f ./id_rsa -N '' -m PEM

run:
	for simulation in GerritGitSimulation GerritRestSimulation; do \
		docker run --rm -ti --env-file simulation.env -v `pwd`/target/gatling:/opt/gatling/results \
			$(DOCKER_IMAGE) -s gerritforge.$$simulation; done

publish: build
	docker push $(DOCKER_IMAGE)