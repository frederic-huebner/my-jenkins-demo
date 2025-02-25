import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

def DATE_TIME = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS").withZone(ZoneId.of("UTC")).format(Instant.now())
def pluginsToReviewManually = []
def pluginsDeprecated = []

pipeline {
    agent any
    options {
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
                    list_jenkins_plugins("${WORKSPACE}/jenkins_auto_update_plugins", "plugins_list_BEFORE-UPDATE_${DATE_TIME}.txt")
(pluginsToReviewManually, pluginsDeprecated) = jenkins_safe_plugins_update()
                    list_jenkins_plugins("${WORKSPACE}/jenkins_auto_update_plugins", "plugins_list_AFTER-UPDATE_${DATE_TIME}.txt")
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

//List all active plugins and save them into a file
def list_jenkins_plugins(directory, fileName) {
    sh(script: "touch ${directory}/${fileName}", returnStatus: true)
    jenkins.model.Jenkins.instance.pluginManager.activePlugins.findAll {
        plugin -> sh(script: "echo ${plugin.getDisplayName()} (${plugin.getShortName()}): ${plugin.getVersion()}\n", returnStatus: true)
    }
}

//Perform jenkins plugin update in a safe manner
def jenkins_safe_plugins_update() {
    //Refresh plugins updates list
    jenkins.model.Jenkins.getInstanceOrNull().getUpdateCenter().getSites().each {
        site -> site.updateDirectlyNow(hudson.model.DownloadService.signatureCheck)
    }
    hudson.model.DownloadService.Downloadable.all().each {
        downloadable -> downloadable.updateNow();
    }
    //Get the list of plugins
    def pluginsToUpdate = []
    def pluginsToReviewManually = []
    def pluginsDeprecated = []
    jenkins.model.Jenkins.instance.pluginManager.activePlugins.findAll {
        plugin -> if (!(plugin.getDeprecations().isEmpty())) {
            pluginsDeprecated.add(plugin.getDisplayName())
        } else if (plugin.hasUpdate()) {
            if (plugin.getActiveWarnings().isEmpty()) {
                pluginsToUpdate.add(plugin.getShortName())
            }
            else {
                pluginsToReviewManually.add(plugin.getDisplayName())
            }
        }
    }
    println "Plugins to upgrade automatically: ${pluginsToUpdate}"
    println "Plugins to review and update manually: ${pluginsToReviewManually}"
    println "Plugins depricated: ${pluginsDeprecated}"
    long count = 0
    jenkins.model.Jenkins.instance.pluginManager.install(pluginsToUpdate, false).each {
        f -> f.get()
        println "${++count}/${pluginsToUpdate.size()}.."
    }
    if (pluginsToUpdate.size() != 0 && count == pluginsToUpdate.size()) {
        jenkins.model.Jenkins.instance.safeRestart()
    }
    return [pluginsToReviewManually, pluginsDeprecated]
}