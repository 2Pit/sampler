**More about the service**  
Libraries: KTor, Kotlinx-serialization
The service gets notification from GitHub App and runs a special pipeline.  
Pipeline is a special KTor object. It can contains one or many phases. And in phase you can run another pipeline.  
For example: A notification with header "x-github-event: installation" runs a pipeline which add a sample to the system.
Consider the basic steps.  
Pipeline 1.

1. Split on repositories. Author of a sample can install GitHub App for all or several repositories.
Therefore, at this phase we parallelize the addition to each repository. 


Pipeline 2.

1. Create an issue in *ksamples/main*
2. Create a card in project board
3. Fork a sample repository
4. Prepare test. 
    Here we have one or many branches in the repository. So we run new pipeline for each branch

Pipeline 3.

1. Test branch


For event **push** we should create new pull request (if it doesn’t exist before) and test it. But this part is unimplemented.
