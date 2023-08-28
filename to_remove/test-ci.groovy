def product_name = 'SIC'
def platform = 's2l_ssc_camera'
def platform_list = 's2l_ssc_camera'
def repo_fw = 'sic_fw'
def repo_app = 'sic_app'

folder('BE')
folder('BE/EMB')
folder("BE/EMB/$product_name")

pipelineJob("BE/EMB/$product_name/${product_name}_SDK") {
  
  description("Build $product_name buildroot SDK")
  
  parameters {
    booleanParam(parameterName='FULL_BUILD', defaultValue=false, description='Check to clean whole project before building. Otherwise it will only rebuild the applicative part of the firmware')
    stringParam(parameterName='FEATURE', defaultValue='None', description='The feature (branch) to select if it exists')
    stringParam(parameterName='BASE_BRANCH', defaultValue='master', description='The branch on which to base the build')
    stringParam(parameterName='PLATFORM', defaultValue="$platform", description="The platform to be built ($platform_list)")
  }
  
  logRotator(daysToKeep = 5, numToKeep = 5, artifactDaysToKeep = -1, artifactNumToKeep = -1)
  
  properties {
    githubProjectUrl("git@github.com:xofym/${repo_fw}.git/")
    
    disableConcurrentBuilds {
      abortPrevious(value=false)
    }
    
    durabilityHint {
      hint(value='PERFORMANCE_OPTIMIZED')
    }
    
    pipelineTriggers {
      triggers {
        cron {
          spec("H 0 * * *")
        }
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
            github(ownerAndProject="xofym/${repo_fw}", protocol='ssh')
          }
          extensions {
            localBranch {
              localBranch(value = 'master')
            }
          }
        }
      }
      lightweight(lightweight = true)
      scriptPath(scriptPath = 'ci/Jenkinsfile.build')
    }
  }
}

pipelineJob("BE/EMB/$product_name/${product_name}_DOCKER") {
  description('Build docker image containing already built builroot sdk and push it to registry')
  
  properties {
    copyArtifactPermission {
      projectNames(value="BE/EMB/$product_name/${product_name}_SDK")
    } 
    
    disableConcurrentBuilds {
      abortPrevious(value=false)
    }
    
    pipelineTriggers {
      triggers {
        upstream {
          upstreamProjects("BE/EMB/$product_name/${product_name}_SDK")
          threshold('SUCCESS')
        }
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
            github(ownerAndProject="xofym/${repo_fw}", protocol='ssh')
          }
          extensions {
            localBranch {
              localBranch(value = 'master')
            }
          }
        }
      }
      lightweight(lightweight = true)
      scriptPath(scriptPath = 'ci/Jenkinsfile.dockersdk')
    }
  }
}

pipelineJob("BE/EMB/$product_name/${product_name}_APP") {
  
  description("Build $product_name application")
  
  parameters {
    stringParam(parameterName='FEATURE', defaultValue='None', description='The feature (branch) to select if it exists')
    stringParam(parameterName='BASE_BRANCH', defaultValue='master', description='The branch on which to base the build')
  }
  
  logRotator(daysToKeep = 5, numToKeep = 5, artifactDaysToKeep = -1, artifactNumToKeep = -1)
  
  properties {
    githubProjectUrl("git@github.com:xofym/${repo_app}.git/")
    
    disableConcurrentBuilds {
      abortPrevious(value=false)
    }
    
    durabilityHint {
      hint(value='PERFORMANCE_OPTIMIZED')
    }
    
    pipelineTriggers {
      triggers {
        upstream {
          upstreamProjects("BE/EMB/$product_name/${product_name}_DOCKER")
          threshold('SUCCESS')
        }
      }
    }
  }
  
  definition {
    cpsScm {
      scm {
        git {
          branch(branch='master')
          remote {
            credentials('github-cimyfox-ssh')
            github(ownerAndProject="xofym/${repo_app}", protocol='ssh')
          }
          extensions {
            localBranch {
              localBranch(value = 'master')
            }
          }
        }
      }
      lightweight(lightweight = true)
      scriptPath(scriptPath = 'ci/Jenkinsfile.build')
    }
  }
}

multibranchPipelineJob("BE/EMB/$product_name/${repo_fw}_CheckBuildOnPR") {
  description("Check build on PR on repository $repo_fw")
  
  branchSources {
    branchSource {
      source {
        github {
          repoOwner('xofym') 
          repository("$repo_fw") 
          repositoryUrl('https://github.com/xofym')
          configuredByUrl(true)
          credentialsId('JenkinsGithub')
          
          traits {
            gitHubPullRequestDiscovery {
              strategyId(2)
            }
            notificationContextTrait {
              contextLabel('ci/check_build')
              typeSuffix(false)
            }
          }
        }
      }
    }
  }
  
  factory {
    workflowBranchProjectFactory {
        scriptPath('ci/Jenkinsfile.pr')
    }
  }
  
  orphanedItemStrategy {
    discardOldItems  {
      numToKeep(5) 
    }
  }
}

multibranchPipelineJob("BE/EMB/$product_name/${repo_app}_CheckBuildOnPR") {
  description("Check build on PR on repo $repo_app")
  
  branchSources {
    branchSource {
      source {
        github {
          repoOwner('xofym') 
          repository("$repo_app") 
          repositoryUrl('https://github.com/xofym')
          configuredByUrl(true)
          credentialsId('JenkinsGithub')
          
          traits {
            gitHubPullRequestDiscovery {
              strategyId(2)
            }
            notificationContextTrait {
              contextLabel('ci/check_build')
              typeSuffix(false)
            }
          }
        }
      }
    }
  }
  
  factory {
    workflowBranchProjectFactory {
        scriptPath('ci/Jenkinsfile.pr')
    }
  }
  
  orphanedItemStrategy {
    discardOldItems  {
      numToKeep(5) 
    }
  }
}
