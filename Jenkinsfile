gatlingVer = "3.2.0"

node {
  def gatlingUrl = "https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/${gatlingVer}/gatling-charts-highcharts-bundle-${gatlingVer}-bundle.zip"

  checkout scm
  try {
    gerritReview labels: [Verified: 0]
    stage('Package') {
      sh 'sbt clean assembly'
      sh "curl ${gatlingUrl} > gatling.zip"
      dir('target') {
        sh "unzip ../gatling.zip"
        sh "mv scala-2.12/*.jar gatling-charts-highcharts-bundle-${gatlingVer}/lib/."
        sh "cp -R ../src/test/scala/* gatling-charts-highcharts-bundle-${gatlingVer}/user-files/simulations/."
        sh "cp ../src/test/resources/* gatling-charts-highcharts-bundle-${gatlingVer}/conf/."
        stash name: "gatling-bundle", includes: "gatling-charts-highcharts-bundle-${gatlingVer}/"
      }
    }
    gerritReview labels: [Verified: 1]
  } catch (e) {
    gerritReview labels: [Verified: -1]
    throw e
  }
}

node('gatling') {
  unstash "gatling-bundle"

  parallel gatling-1: {
    stage('Run GerritGitSimulation load-test') {
      dir("target/gatling-charts-highcharts-bundle-${gatlingVer}") {
        sh "./bin/gatling.sh -s gerritforge.GerritGitSimulation"
      }
    }
  },
  gatling-2: {
    stage('Run GerritGitSimulation load-test') {
      unstash "gatling-bundle"
      dir("target/gatling-charts-highcharts-bundle-${gatlingVer}") {
        sh "./bin/gatling.sh -s gerritforge.GerritGitSimulation"
      }
    }
  }

  archiveArtifacts artifacts: 'results/**/*'
}