import utilities.MultibranchJobBuilder
import utilities.FirmwareBuildJobBuilder
import groovy.json.JsonSlurper


def projects = new JsonSlurper().parseText(readFileFromWorkspace('projectDescription.json'))

base_dir = projects.baseDirectory

def create_base_folder(dslFactory, dir) {
	String[] subdirs = dir.split("/")
	def cur = subdirs[0]
	for (String subdir : subdirs) {
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

FirmwareBuildJobBuilder.generate_build_server_docker(this, base_dir)

projects.multibranch.each { key, _ ->
	projects.multibranch[key].each { job -> 
		def builder = new MultibranchJobBuilder(base_dir, key, job)
		builder.generate_multibranch(this)
	}
}

projects.jobs.each { key, _ ->
	projects.jobs[key].each { job -> 
		def builder = new FirmwareBuildJobBuilder(this, base_dir, key, job)
		builder.generate_pipeline(this)
	}
}
