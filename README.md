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
> gatling:testOnly gerritforge.GatlingRestSimulation
```

List all tasks
--------------------

```bash
> tasks gatling -v
```

Run tests in Docker
-------------------

The top-level Makefile allows to package all tests into a Docker image and push
to a Docker registry. The scenario is useful for running Gatling tests on a cloud
setup (e.g. AWS or similar) and scale them up to multiple workers.

For building and publishing the tests into a Docker image:

```bash
$ make push
```

For running the tests from Docker, with the environment variables defined in
`simulation.env`:

```
$ make run
```
