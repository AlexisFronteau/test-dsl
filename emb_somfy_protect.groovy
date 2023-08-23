import utilities.MultibranchJobBuilder
import groovy.json.JsonSlurper

def projects = new JsonSlurper().parseText(readFileFromWorkspace('projectDescription.json'))
def base_dir = 'BE/EMB'

folder('BE')
folder('BE/EMB')

projects.multibranch.each { multibranch ->
	def path = base_dir
	if (multibranch.path) {
		path += ('/' + multibranch.path)
	}
	def repo_name = multibranch.repo
	
	def mutlibranchType = eMutlibranchType.UNKNOWN
	
	if (multibranch.type == 'build') {
		mutlibranchType = eMutlibranchType.BUILD
	}
	else if (multibranch.type == 'clang-format') {
		mutlibranchType = eMutlibranchType.CLANG_FORMAT
	}
	else if (multibranch.type == 'unit-tests') {
		mutlibranchType = eMutlibranchType.UNIT_TESTS
	}
	
	MultibranchJobBuilder.multibranch(this, mutlibranchType, path, repo_name)
}
