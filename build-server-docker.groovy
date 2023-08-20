def repo_name = 'BE-CI-Tools'

folder('BE')
folder('BE/EMB')
folder('BE/EMB/JENKINS_UTILS')

pipelineJob('BE/EMB/JENKINS_UTILS/BUILD_SERVER_DOCKER') {
  description("Build docker image that will be used by CI")
  
  properties {
    disableConcurrentBuilds {
      abortPrevious(value=false)
    }
    
    durabilityHint {
      hint(value='PERFORMANCE_OPTIMIZED')
    }
    
    pipelineTriggers {
      triggers {
        githubPush()
      }
    }
  }
  
  definition {
    cpsScm {
      scm {
        git {
          branch(branch='master')
          remote {
            credentials(credentials='jenkins-github-ssh')
            github(ownerAndProject="xofym/${repo_name}", protocol='ssh')
          }
          extensions {
            pathRestriction {
              includedRegions('jenkins_pipelines/docker_fw_build/.*\njenkins_slave_builder/.*')
              excludedRegions('')
            }
          }
        }
      }
      lightweight(lightweight = true)
      scriptPath(scriptPath = 'jenkins_pipelines/docker_fw_build/Jenkinsfile')
    }
  }
}
