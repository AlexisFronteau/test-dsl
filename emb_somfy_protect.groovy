import utilities.CheckClangFormatOnPRMultibranchJobBuilder
import groovy.json.JsonSlurper

def groovyUtils = new com.eviware.soapui.support.GroovyUtils(context)
def projectDir = groovyUtils.projectPath

evaluate(new File(projectDir, "utilities/builder_multibranch_checkClangFormatOnPR.groovy"))

def projects = new JsonSlurper().parseText(readFileFromWorkspace('projectDescription.json'))
def base_dir = 'BE/EMB'

folder('BE')
folder('BE/EMB')

projects.multibranch.checkClangFormatOnPR.each { multibranch ->
	def path = base_dir 
	if (multibranch.path) {
		path += ('/' + multibranch.path)
	}
	def repo_name = multibranch.repo
	
	CheckClangFormatOnPRMultibranchJobBuilder.multibranch(this, path, repo_name)
}
