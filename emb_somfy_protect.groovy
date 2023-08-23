import utilities.MultibranchJobBuilder
import groovy.json.JsonSlurper

def projects = new JsonSlurper().parseText(readFileFromWorkspace('projectDescription.json'))
def base_dir = 'BE/EMB'

folder('BE')
folder('BE/EMB')

projects.multibranch.each { multibranch ->
	if (!multibranch.repo)
	{
		println("Missing repository name");
		return
	}

	def repo_name = multibranch.repo

	def path = base_dir
	if (multibranch.path) {
		path += ('/' + multibranch.path)
	}

	def mutlibranchType = MULTIBRANCH_UNKNOWN

	if (multibranch.type) {
		if (multibranch.type == 'build') {
			mutlibranchType = MULTIBRANCH_BUILD
		}
		else if (multibranch.type == 'clang-format') {
			mutlibranchType = MUTLIBRANCH_CLANG_FORMAT
		}
		else if (multibranch.type == 'unit-tests') {
			mutlibranchType = MULTIBRANCH_UNIT_TESTS
		}
		else
		{
			println("Invalid multibranch type " + multibranch.type + " for " + multibranch.repo)
			return
		}
	}
	else {
		println("No multibranch type for " + multibranch.repo)
		return
	}
	
	MultibranchJobBuilder.multibranch(this, mutlibranchType, path, repo_name)
}
