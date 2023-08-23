package utilities

/**
 * Generates jobs that build firmware sdk
 */
class CheckOnBuildMultibranchJobBuilder {

  /**
   * Generates a Pipeline job to deploy a staging environment
   */
  static void multibranch(dslFactory, dirpath, repo_name) {
    dslFactory.folder(dirpath)

    def job = dslFactory.pipelineJob()
    
    job.with {
      folder(dirpath)
      
      multibranchPipelineJob(dirpath + '/' + repo_name + '_CheckBuildOnPR') {
        description('Check build on PR on repository ' + repo_name)
        
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
                    contextLabel('ci/check-build')
                    typeSuffix(false)
                  }
                }
              }
            }
          }
        }
        
        factory {
          workflowBranchProjectFactory {
            scriptPath('ci/Jenkinsfile.clang-format')
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
