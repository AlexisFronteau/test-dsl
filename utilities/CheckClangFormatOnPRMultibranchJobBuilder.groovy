package utilities
import javaposse.jobdsl.dsl.DslFactory

/**
 * Generates jobs that check if code is correclty formatted
 */
class CheckClangFormatOnPRMultibranchJobBuilder {
  static void multibranch(dslFactory, dirpath, repo_name) {
    def job = dslFactory.multibranchPipelineJob()
    job.with {
      folder(dirpath)
      
      multibranchPipelineJob(dirpath + '/' + repo_name + '_CheckClangFormatOnPR') {
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
                    contextLabel('ci/check-format')
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
    }
  }
}
