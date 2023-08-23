import utilities.MultibranchJobBuilder
import groovy.json.JsonSlurper

base_dir = 'BE/EMB'

def projects = new JsonSlurper().parseText(readFileFromWorkspace('projectDescription.json'))

def build_repository_path(repo_name, job_subpath) {
	if (!repo_name)
	{
		throw new Exception("Missing repository name")
	}

	def path = base_dir
	if (job_subpath) {
		path += ('/' + job_subpath)
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

projects.multibranch.unittests.each { multibranch ->
	def (repo_name, path) = build_repository_path(multibranch.repo, multibranch.path)
	MultibranchJobBuilder.multibranch(this, "unit-tests", path, repo_name)
}