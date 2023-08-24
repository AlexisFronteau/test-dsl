import utilities.MultibranchJobBuilder
import utilities.FirmwareBuildJobBuilder
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

def create_base_folder(dslFactory, dir) {
	String[] subdirs = dir.split("/");
	def cur = subdirs[0]
	for (String subdir : subdirs)
	{
		if (cur == '') {
			cur = subdir
		}
		else {
			cur += '/' + subdir
		}

		dslFactory.folder(cur)
	}
}

create_base_folder(this, base_dir)

projects.multibranch.build.each { multibranch ->
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

projects.jobs.build.each { job ->
	def builder = new FirmwareBuildJobBuilder(base_dir, job)
	builder.generate_pipeline(this)
}