package utilities

/**
 * Generates jobs that check if code is correclty formatted
 */
class MultibranchJobBuilder {
  static void multibranch(dslFactory, type, dirpath, repo_name) {

    dslFactory.folder(dirpath)

    def jobSuffix = ''
    def githubLabel = ''

    if (type) {
      if (type == 'build') {
        jobSuffix = 'checkBuildOnPR'
        githubLabel = 'build'
      }
      else if (type == 'clang-format') {
        jobSuffix = 'checkClangFormatOnPR'
        githubLabel = 'clang-format'
      }
      else if (type == 'unit-tests') {
        jobSuffix = 'checkUnitTestsOnPR'
        githubLabel = 'unit-tests'
      }
      else
      {
        println("Invalid multibranch type " + type + " for " + repo_name)
        return
      }
    }
    else {
      println("No multibranch type for " + repo_name)
      return
    }

    def job = dslFactory.multibranchPipelineJob(dirpath + '/' + repo_name + '_' + jobSuffix)
    job.with {
      description('Check clang-format on PR on repository ' + repo_name)

      disabled()
      
      branchSources {
        branchSource {
          source {
            github {
              repoOwner('xofym') 
              repository(repo_name) 
              repositoryUrl('https://github.com/xofym')
              configuredByUrl(true)
              credentialsId('JenkinsGithub')
              
              traits {
                gitHubPullRequestDiscovery {
                  strategyId(2)
                }
                notificationContextTrait {
                  contextLabel('ci/check-' + githubLabel)
                  typeSuffix(false)
                }
              }
            }
          }
        }
      }

      factory {
        workflowBranchProjectFactory {
          scriptPath('ci/Jenkinsfile.' + jobSuffix)
        }
      }

      orphanedItemStrategy {
        discardOldItems  {
          numToKeep(5) 
        }
      }
    }
  }
}
