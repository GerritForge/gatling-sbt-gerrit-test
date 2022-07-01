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
		docker run -e JAVA_OPTS="-Xmx4g" --rm --env-file simulation.env ${GERRIT_PROJECT_ENV} -v "$$(pwd)/target/gatling:/opt/gatling/results" \
			--add-host=host.docker.internal:host-gateway \
			$(DOCKER_IMAGE) -s gerritforge.$$simulation; done

background-job-%:
	mkdir -p `pwd`/target/gatling
	if [ ! -z "${GERRIT_PROJECT}" ]; then GERRIT_PROJECT_ENV="-e GERRIT_PROJECT=${GERRIT_PROJECT}" make run &> `pwd`/target/gatling/$@.log; else make run &> `pwd`/target/gatling/$@.log; fi &

parallel-run:
	for i in $$(seq 1 ${JOBS}); do \
		if [ ! -z "${REPO_PREFIX}" ]; then GERRIT_PROJECT="${REPO_PREFIX}_$$i" make background-job-$$i; else make background-job-$$i; fi; done

push: build
	sbt dockerBuildAndPush
