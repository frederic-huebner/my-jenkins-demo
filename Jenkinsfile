import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

def DATE_TIME = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS").withZone(ZoneId.of("UTC")).format(Instant.now())
def pluginsToReviewManually = []
def pluginsDeprecated = []
pipeline {
    agent any
    options {
        //Build options
        disableConcurrentBuilds()
        buildDiscarder(
            logRotator (
                artifactDaysToKeepStr: '10',
                artifactNumToKeepStr: '1',
                daysToKeepStr: '30',
                numToKeepStr: '30'
            )
        )
    }
    triggers {
        cron('0 0 * * 7')
    }
    stages {
        stage('Update_Plugins') {
            steps {
                script {
                    def safePluginUpdateModule = load("${WORKSPACE}/jenkins_auto_update_plugins/jenkins-plugins-uptodate.groovy")
                    safePluginUpdateModule.list_jenkins_plugins("${WORKSPACE}/jenkins_auto_update_plugins", "plugins_list_BEFORE-UPDATE_${DATE_TIME}.txt")
                    (pluginsToReviewManually, pluginsDeprecated) = safePluginUpdateModule.jenkins_safe_plugins_update()
                    safePluginUpdateModule.list_jenkins_plugins("${WORKSPACE}/jenkins_auto_update_plugins", "plugins_list_AFTER-UPDATE_${DATE_TIME}.txt")
                }
            }
        }
    }
    post {
        always {
            script {
                archiveArtifacts "jenkins_auto_update_plugins/plugins_list_*_${DATE_TIME}.txt"
                if (!(pluginsToReviewManually.isEmpty())) {
                    echo "IMPORTANT!!! The following plugins need to get reviewed and updated manually: ${pluginsToReviewManually}"
                } else if (!(pluginsDeprecated.isEmpty())) {
                    echo "IMPORTANT!!! The following plugins are deprecated and need to be deleted: ${pluginsDeprecated}"
                }
            }
        }
        failure {
            echo "${JOB_BASE_NAME} faild!"
        }
    }
}
