package utilities

/**
 * Generates jobs that check if code is correclty formatted
 */
enum eMutlibranchType {
  CLANG_FORMAT,
  BUILD,
  UNIT_TESTS,
  UNKNOWN
}

class CheckClangFormatOnPRMultibranchJobBuilder {
  static void multibranch(dslFactory, type, dirpath, repo_name) {

    dslFactory.folder(dirpath)

    def jobSuffix = ''
    def githubLabel = ''

    switch(type) {
      case CLANG_FORMAT:
        jobSuffix = 'checkClangFormatOnPR'
        githubLabel = 'clang-format'
      break
      case CLANG_FORMAT:
        jobSuffix = 'checkBuildOnPR'
        githubLabel = 'build'
      break
      case CLANG_FORMAT:
        jobSuffix = 'checkUnitTestsOnPR'
        githubLabel = 'unit-tests'
      break
      case UNKNOWN:
      return
    }

    def job = dslFactory.multibranchPipelineJob(dirpath + '/' + repo_name + '_' + jobSuffix)
    job.with {
      description('Check clang-format on PR on repository ' + repo_name)
      
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
