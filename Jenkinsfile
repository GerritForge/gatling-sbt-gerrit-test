node {
  checkout scm
  try {
    gerritReview labels: [Verified: 0]
    stage('Compile') {
      sh 'sbt compile'
    }
    gerritReview labels: [Verified: 1]
  } catch (e) {
    gerritReview labels: [Verified: -1]
    throw e
  }
}