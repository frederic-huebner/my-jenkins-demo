#!usr/bin/env groovy
pipeline {
    agent none

    stages {
        stage('Hello') {
            agent {
                docker {
                    image 'alpine:latest'
                }
            }
            steps {
                sh 'echo "Hello, World!"'
            }
        }
    }
}