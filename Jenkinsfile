node {
  checkout scm
  try {
    gerritReview labels: [Verified: 0]
    stage('Compile') {
      sh 'sbt compile'
    }
    stage('Package') {
      sh 'sbt assembly'
    }
    gerritReview labels: [Verified: 1]
  } catch (e) {
    gerritReview labels: [Verified: -1]
    throw e
  }
}