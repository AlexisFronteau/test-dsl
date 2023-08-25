package utilities

/**
 * Generates jobs that check if code is correclty formatted
 */
class MultibranchJobBuilder {
  String m_sDirectory = ''
  String m_sRepoName = ''
  String m_sType = ''
  String m_sJobSuffix = ''

  MultibranchJobBuilder(base_dir, type, json) {
    if (!json.keySet().contains('repo'))
    {
      throw new Exception("Missing repository name")
    }

    m_sRepoName = json.repo

    m_sDirectory = base_dir
    if (json.keySet().contains('path')) {
      m_sDirectory += ('/' + json.path)
    }

    m_sType = type

    switch (m_sType) {
      case 'build':
        m_sJobSuffix = 'checkBuildOnPR'
        break
      case 'clang-format':
        m_sJobSuffix = 'checkClangFormatOnPR'
        break
      case 'unit-tests':
        m_sJobSuffix = 'checkUnitTestsOnPR'
        break
      default:
        throw new Exception("Invalid multibranch type " + m_sType + " for " + m_sRepoName)
    }

  }

  void generate_multibranch(dslFactory) {

    dslFactory.folder(m_sDirectory)

    def job = dslFactory.multibranchPipelineJob(m_sDirectory + '/' + m_sRepoName + '_' + m_sJobSuffix)
    job.with {
      description('Check clang-format on PR on repository ' + m_sRepoName)

      branchSources {
        branchSource {
          source {
            github {
              repoOwner('xofym') 
              repository(m_sRepoName) 
              repositoryUrl('https://github.com/xofym')
              configuredByUrl(true)
              credentialsId('JenkinsGithub')
              
              traits {
                gitHubPullRequestDiscovery {
                  strategyId(2)
                }
                notificationContextTrait {
                  contextLabel('ci/check-' + m_sType)
                  typeSuffix(false)
                }
              }
            }
          }
        }
      }

      factory {
        workflowBranchProjectFactory {
          scriptPath('ci/Jenkinsfile.' + m_sJobSuffix)
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
