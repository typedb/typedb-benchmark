Repository to test grabl functionality in a simple and close to real world usage way.

## Usage

Do **not** commit any code, commits should be empty and serve only the purpose of triggering automation.

To make an empty commit use the following command:
`git commit --allow-empty -m "trigger Grabl CI"`

==================================================================================

*main* - commit to this branch to trigger the job that will make commits to 3 other branches - *job-execution*, *simulation* and *dependency-analysis*, and combine all the results into one.

Should be used to test overall functionality of grabl in 1 commit.

==================================================================================

*job-execution* - commit to this branch to trigger set of general purpose jobs.
It will trigger all types of pipelines and all type of workflows, except `performance-analysis` and `dependency-analysis`.
The job set will contain: job with large log file, failed jobs, jobs run in parrallel and jobs run one after another, release jobs.

**Structure**: 
6 workflows: *quality*, *correctness*, *performance*, *validation*, *deployment*, *broadcast*

**quality** contains filter and 3 job: 

  **quality-1** with a filter
  **quality-2** filtered out by the owner filter
  **quality-3** filtered out by the branch filter
  
**correctness** contains 4 job: 

  **correctness-1-a**  | run in background
                       |                       those 2 jobs run in parrallel
  **correctness-1-b**  | run in foreground 
  **correctness-2** run after **correctness-1-a** and **correctness-1-b**
  **correctness-3** run after **correctness-2**
  
  
**performance** contains 1 job:

  **performance-analysis** wich does NOT contain performance analysis itself, but a dummy job that produces 10 000 line 
  
  
**validation** contains 1 job: **validation-1**
**deployment** contains 1 job: **deployment-1**
**broadcast** contains 1 job: **broadcast-1**

Should be used to test overall functionality of job execution, Grabl UI and release functionality.

==================================================================================

*simulation* - commit to this branch to trigger simulation job.
It will pull the latest simulation from the repo `graknlabs/simulation` and run it.

Should be used to test grabl tracing, performance related functionality and UI of performance analysis.

==================================================================================


*dependency-analysis* - commit to this branch to trigger dependency analysis job.
It will run multiple dependency analyses - valid, up to date and out of date.

Should be used to test dependency analyses functionality and related UI in Grabl.

==================================================================================
