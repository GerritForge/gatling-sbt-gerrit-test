Gatling's SBT Gerrit Test
=========================

A simple project showing how to configure and use Gatling's SBT plugin to run Gatling simulations
against Gerrit Code Review.

This project uses SBT 1, which is available [here](https://www.scala-sbt.org/download.html).

Pre-requisites
--------------

Run Gerrit v3.2.x with a project available for load-testing purposes and
create a user with a well-known password and associated SSH key.
The project needs to have force-push and create-reference permissions enabled
in the ACLs for the load-test user.

The test is destructive, do not use any existing production projects for load-testing
purposes.

The test rely on a HTTP/Cookie authenticaton for the GUI-based testing and on
user/password and SSH PublicKey authentication for the GIT-based testing.

Configuration
-------------

The simulations use the following environment variable to run the tests:

Variable | Description | Sample
---------|-------------|---------
 GERRIT_HTTP_URL | Gerrit GUI URL | http://host.docker.internal:8080
 GERRIT_SSH_URL | Gerrit SSH URL | ssh://admin@host.docker.internal:29418
 GERRIT_PROJECT | Gerrit project for load test | load-test
 ACCOUNT_COOKIE | HTTP Cookie to access the Gerrit GUI | aSceprr3WikzGrfwg2PvpOhMMcH5qp3ehW
 XSRF_TOKEN | XSRF_TOKEN Cookie to access the Gerrit GUI for pOST operations | 7d02b54405863778dfe1
 GIT_HTTP_USERNAME | Username for Git/HTTP testing | admin
 GIT_HTTP_PASSWORD | Password for Git/HTTP testing | secret
 NUM_USERS | Number of concurrent user sessions | 10
 DURATION | Total duration of the test | 2 minutes

Get the project
---------------

```bash
$ git clone https://github.com/gerritforge/gatling-sbt-gerrit-test.git && cd gatling-sbt-gerrit-test
```

Start SBT
---------
```bash
$ sbt
```

Run all simulations
-------------------

```bash
> gatling:test
```

Run a single simulation
-----------------------

```bash
> gatling:testOnly gerritforge.GerritRestSimulation
```

List all tasks
--------------------

```bash
> tasks -v gatling
```

Run tests in Docker
-------------------

The top-level Makefile allows to package all tests into a Docker image and push
to a Docker registry. The scenario is useful for running Gatling tests on a cloud
setup (e.g. AWS or similar) and scale them up to multiple workers.

For building the tests into a Docker image:

```bash
$ make build
```

For running the tests from Docker, with the environment variables defined in
`simulation.env`:

```
$ make run
```

For making a parallel execution of multiple runs with concurrent Docker:

```
$ make JOBS=2 parallel-run
```

*NOTE: When running multiple jobs in parallel, the make command would
end immediately, and the simulations will continue to run in the background.
Each parallel simulation job logs a `background-job-<n>.log`.*
