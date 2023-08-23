import utilities.MultibranchJobBuilder
import groovy.json.JsonSlurper

def projects = new JsonSlurper().parseText(readFileFromWorkspace('projectDescription.json'))
def base_dir = 'BE/EMB'

def build_repository_path(repo_name) {
	if (!multibranch.repo)
	{
		throw new Exception("Missing repository name")
	}

	def path = base_dir
	if (multibranch.path) {
		path += ('/' + multibranch.path)
	}

	if (!multibranch.type) {
		throw new Exception("Missing repository name")
	}

	return [repo_name, path]
}

folder('BE')
folder('BE/EMB')

projects.multibranch.build.each { multibranch ->
	// if (!multibranch.repo)
	// {
	// 	println("Missing repository name");
	// 	return
	// }
	// 
	// def repo_name = multibranch.repo
	// 
	// def path = base_dir
	// if (multibranch.path) {
	// 	path += ('/' + multibranch.path)
	// }
	// 
	// if (!multibranch.type) {
	// 	println("Missing multibranch type")
	// 	return
	// }

	def (repo_name, path) = build_repository_path(multibranch.repo, multibranch.path)
	MultibranchJobBuilder.multibranch(this, "build", path, repo_name)
}

projects.multibranch.clangformat.each { multibranch ->
	def (repo_name, path) = build_repository_path(multibranch.repo, multibranch.path)
	MultibranchJobBuilder.multibranch(this, "clang-format", path, repo_name)
}

projects.multibranch.clangformat.each { multibranch ->
	def (repo_name, path) = build_repository_path(multibranch.repo, multibranch.path)
	MultibranchJobBuilder.multibranch(this, "unit-tests", path, repo_name)
}