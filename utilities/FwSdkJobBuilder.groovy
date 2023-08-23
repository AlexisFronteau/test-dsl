package utilities

/**
 * Generates jobs that build firmware sdk
 */
class FwSdkJobBuilder {

  /**
   * Generates a Pipeline job to deploy a staging environment
   */
  static void pipeline(dslFactory, product_name, repo_name, platorm, platform_list) {
    def job = dslFactory.pipelineJob(product_name)
    job.with {
      folder('BE')
      folder('BE/EMB')
      folder("BE/EMB/" + product_name)

      pipelineJob("BE/EMB/+" product_name + "/" + product_name + "_SDK") {
        
        description("Build " + product_name + "buildroot SDK")
        
        parameters {
          booleanParam(parameterName='FULL_BUILD', defaultValue=false, description='Check to clean whole project before building. Otherwise it will only rebuild the applicative part of the firmware')
          stringParam(parameterName='FEATURE', defaultValue='None', description='The feature (branch) to select if it exists')
          stringParam(parameterName='BASE_BRANCH', defaultValue='master', description='The branch on which to base the build')
          stringParam(parameterName='PLATFORM', defaultValue=platform, description="The platform to be built (" + platform_list + ")")
        }
        
        logRotator(daysToKeep = 5, numToKeep = 5, artifactDaysToKeep = -1, artifactNumToKeep = -1)
        
        properties {
          githubProjectUrl("git@github.com:xofym/" + repo_name + ".git/")
          
          disableConcurrentBuilds {
            abortPrevious(value=false)
          }
          
          durabilityHint {
            hint(value='PERFORMANCE_OPTIMIZED')
          }
        }
        
        triggers {
          cron {
            spec("H 0 * * *")
          }
        }
        
        definition {
          cpsScm {
            scm {
              git {
                branch(branch='master')
                remote {
                  credentials(credentials='jenkins-github-ssh')
                  github(ownerAndProject="xofym/" + repo_name, protocol='ssh')
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
    }
  }
}
