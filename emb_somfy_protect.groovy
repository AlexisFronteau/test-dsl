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

	if (!multibranch.type) {
		println("Missing multibranch type")
		return
	}
	
	MultibranchJobBuilder.multibranch(this, multibranch.type, path, repo_name)
}
