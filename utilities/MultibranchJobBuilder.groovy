package utilities

/**
 * Generates jobs that check if code is correclty formatted
 */
public enum eMutlibranchType {
  CLANG_FORMAT,
  BUILD,
  UNIT_TESTS,
  UNKNOWN
}

public static final int MUTLIBRANCH_CLANG_FORMAT = 0;
public static final int MULTIBRANCH_BUILD   = 2;
public static final int MULTIBRANCH_UNIT_TESTS  = 3;
public static final int MULTIBRANCH_UNKNOWN  = 4;

class CheckClangFormatOnPRMultibranchJobBuilder {
  static void multibranch(dslFactory, type, dirpath, repo_name) {

    dslFactory.folder(dirpath)

    def jobSuffix = ''
    def githubLabel = ''

    switch(type) {
      case MUTLIBRANCH_CLANG_FORMAT:
        jobSuffix = 'checkClangFormatOnPR'
        githubLabel = 'clang-format'
      break
      case MULTIBRANCH_BUILD:
        jobSuffix = 'checkBuildOnPR'
        githubLabel = 'build'
      break
      case MULTIBRANCH_UNIT_TESTS:
        jobSuffix = 'checkUnitTestsOnPR'
        githubLabel = 'unit-tests'
      break
      default:
        println("Invalid multibranch type")
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
