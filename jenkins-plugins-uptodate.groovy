import java.nio.file.Files
import java.nio.file.Paths

//List all active plugins and save them into a file
def list_jenkins_plugins(directory, fileName) {
    Files.createDirectories(Paths.get(directory))
    File pluginsListFile = new File("${directory}/${fileName}")
    jenkins.model.Jenkins.instance.pluginManager.activePlugins.findAll {
        plugin -> pluginsListFile.append("${plugin.getShortName()}:${plugin.getVersion()}" 
            + System.getProperty("line.separator"))
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
    println "Plugins deprecated: ${pluginsDeprecated}"
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

return this
