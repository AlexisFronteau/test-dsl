package utilities

/**
 * Generates jobs that check if code is correclty formatted
 */

class FirmwareBuildJobBuilder {

    String m_sDirectory = ''
    String m_sJobName = ''
    String m_sRepo_name = ''
    boolean m_bLaunchNightly = true
    String m_sLaunchAfterJob = ''
    String m_sDefaultBaseBranch = 'master'
    String m_sType = ''
    boolean m_bHasDefaultParams = true

    FirmwareBuildJobBuilder(base_dir, type, json) {
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

        m_sType = type

        if (json.launchAfterJob) {
            m_sLaunchAfterJob = json.launchAfterJob
        }

        if (json.launchNightly) {
            m_bLaunchNightly = json.launchNightly
        }

        if (json.hasDefaultParams) {
            m_bHasDefaultParams = hasDefaultParams
        }

        if (json.baseBranch) {
            m_sDefaultBaseBranch = json.baseBranch
        }
    }

    void generate_pipeline(dslFactory) {

        dslFactory.folder(m_sDirectory)

        def job = dslFactory.pipelineJob(m_sDirectory + '/' + m_sJobName)

        if (m_bHasDefaultParams)
        {
            job.with {
                parameters {
                    stringParam {
                        name('FEATURE')
                        defaultValue('None')
                        description('The feature (branch) to select if it exists')
                        trim(false)
                    }

                    stringParam {
                        name('BASE_BRANCH')
                        defaultValue("${m_sDefaultBaseBranch}")
                        description('The branch on which to base the build')
                        trim(false)
                    }
                }
            }
        }

        job.with {
            description("Build firmware from ${m_sRepo_name} after ${m_sLaunchAfterJob} and ${m_bLaunchNightly}")
    
            disabled(true)

            // parameters {
            //     booleanParam(parameterName='FULL_BUILD', defaultValue=false, description='Check to clean whole project before building. Otherwise it will only rebuild the applicative part of the firmware')
            //     stringParam(parameterName='FEATURE', defaultValue='None', description='The feature (branch) to select if it exists')
            //     stringParam(parameterName='BASE_BRANCH', defaultValue='master', description='The branch on which to base the build')
            //     stringParam(parameterName='PLATFORM', defaultValue="$platform", description="The platform to be built ($platform_list)")
            // }
            
            logRotator {
                numToKeep(5)
                daysToKeep(-1)
                artifactDaysToKeep(-1)
                artifactNumToKeep(-1)
            }

            properties {
                disableConcurrentBuilds {
                    abortPrevious(false)
                }
                
                durabilityHint {
                    hint('PERFORMANCE_OPTIMIZED')
                }

                if (m_bLaunchNightly == true) {
                    pipelineTriggers {
                        triggers {
                            cron {
                                spec("H 0 * * *")
                            }
                        }
                    }
                }

                if (!m_sLaunchAfterJob.isEmpty()) {
                    pipelineTriggers {
                        triggers {
                            upstream {
                                upstreamProjects("${m_sLaunchAfterJob}")
                                threshold('SUCCESS')
                            }
                        }
                    }
                }
            }
            
            definition {
                cpsScm {
                    scm {
                        git {
                            branch("${m_sDefaultBaseBranch}")
                            remote {
                                credentials('jenkins-github-ssh')
                                github("xofym/${m_sRepo_name}", 'ssh', 'github.com')
                            }
                            extensions {
                                localBranch {
                                    localBranch("${m_sDefaultBaseBranch}")
                                }
                            }
                        }
                    }
                    lightweight(true)
                    scriptPath('ci/Jenkinsfile.' + m_sType)
                }
            }
        }

        // if (m_bLaunchNightly == true) {
        //     job.with {
        //         properties {
        //             pipelineTriggers {
        //                 triggers {
        //                     cron {
        //                         spec("H 0 * * *")
        //                     }
        //                 }
        //             }
        //         }
        //     }
        // }

        // if (!m_sLaunchAfterJob.isEmpty()) {
        //     job.with {
        //         properties {
        //             pipelineTriggers {
        //                 triggers {
        //                     upstream {
        //                         upstreamProjects("${m_sLaunchAfterJob}")
        //                         threshold('SUCCESS')
        //                     }
        //                 }
        //             }
        //         }
        //     }
        // }
    }
}