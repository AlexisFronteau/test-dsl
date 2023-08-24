package utilities

/**
 * Generates jobs that check if code is correclty formatted
 */

class FirmwareBuildJobBuilder {

    String m_sDirectory = ''
    String m_sJobName = ''
    String m_sRepo_name = ''
    boolean m_launchNightly = true
    boolean m_launchAfterJob = ''
    String m_defaultBaseBranch = 'master'

    FirmwareBuildJobBuilder(base_dir, json) {
        m_sDirectory = base_dir
        if (json.path) {
            m_sDirectory += ('/' + json.path)
        }

        if (json.jobName) {
            m_sJobName = json.jobName
        }
        else if (json.repo) {
            m_sJobName = json.repo.upper()
        }

        if (json.repo) {
            m_sRepo_name = json.repo
        }
    }

    static void generate_pipeline(dslFactory) {

        dslFactory.folder(m_sDirectory)

        def job = dslFactory.pipelineJob(m_sDirectory + '/' + m_sJobName)

        if (m_launchNightly == true) {
            job.with {
                properties {
                    pipelineTriggers {
                        triggers {
                            cron {
                                spec("H 0 * * *")
                            }
                        }
                    }
                }
            }
        }

        if (m_launchAfterJob != '') {
            job.with {
            pipelineTriggers {
                    triggers {
                        upstream {
                            upstreamProjects(launchAfterJob)
                            threshold('SUCCESS')
                        }
                    }
                }
            }
        }

        job.with {
            description("Build firmware from ${repo_name}")
    
            // parameters {
            //     booleanParam(parameterName='FULL_BUILD', defaultValue=false, description='Check to clean whole project before building. Otherwise it will only rebuild the applicative part of the firmware')
            //     stringParam(parameterName='FEATURE', defaultValue='None', description='The feature (branch) to select if it exists')
            //     stringParam(parameterName='BASE_BRANCH', defaultValue='master', description='The branch on which to base the build')
            //     stringParam(parameterName='PLATFORM', defaultValue="$platform", description="The platform to be built ($platform_list)")
            // }
            
            logRotator(daysToKeep = 5, numToKeep = 5, artifactDaysToKeep = -1, artifactNumToKeep = -1)
            
            properties {
                githubProjectUrl("git@github.com:xofym/${repo_name}.git/")
                
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
                                github(ownerAndProject="xofym/${repo_name}", protocol='ssh')
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