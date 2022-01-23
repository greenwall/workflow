# Overall

DOMPAP is a Java library for building and executing workflows.

A workflow is a long running process implemented as a plain Java class.
The process is checkpointed (storing state) after each succesful method/step.
Execution may include calling services (both internal and external) and end user activities.

# Purpose

DOMPAP supports long-running business processes (workflow) involving delivering and feedback from multiple systems and multiple end users.

The design is based on these principles:
* Reliable service orchestration - a workflow must never be lost. Partial or complete system failures including DOMPAP itself must be expected and recovered.
* Transparent to end user - End users must be able to query the status of workflows to check what happened when. No need to involve operations. 
* Widely accepted technology - Developing, testing and deploying workflows must be possible for any Java developer with minimum introduction to DOMPAP conventions.
* Horizontally scalable - Workflow engines should be separately deployable and isolated from each other so that new workflows can be tested and deployed independently.  

[System Architecture](https://wiki.itgit.oneadr.net/display/NTOS/Dompap+System+Architecture)

Dompap executes a series of steps forming a workflow.
* After each step the state of the workflow is persisted (check pointing).
* Any failing steps are retried a defined number of times. 
* A step can be a callout to a process, a program, a system, updating a table or just updating the state of the workflow.

# Developers Guide

See confluence
[Developers Guide](https://wiki.itgit.oneadr.net/display/NTOS/DOMPAP+Developers+Guide)


## Simple example workflow
````java
// MyWorkflow must be Serializable in order to persist it's state.
public class MyWorkflow implements Serializable {
    private int number;
    private String message;

    public Method stepA() {
        // Call some external service. 
        // If this call fails the default retry mechanism will repeat execution of stepA a number of times before giving up.
        message = callSomeExternalService(number);

        // stepB is defined as a static final Method constant
        return stepB;   
    }

    // By annotating method with @FinalState (and returning null) the engine knows that 
    // no further methods will be executed after this and this workflow is marked for archiving in 30 days (default).
    private static Method stepB = method("stepB"); // Definition of the method.
    @FinalState
    public Method stepB() {
        // Call another service
        callAnotherService(message);

        // Let the engine know that no further method is executed. 
        // In combination with the @FinalState the engine will mark the workflow for archiving.
        return null;
    }
} 
````


## Postponing method execution

In order to delay execution of a method the previous method may return a WhenMethod specifying when the method should be executed.

````java
public class SomeWorkflow {
    // ...
    public WhenMethod stepA() {
        // Do whatever
        return methodWhen(stepB, executionTime);
    }
    
    public Method stepB() {
        // At a later time execute something.
        return nextStep;        
    }
}
````

## Accessing current workflow 

The current workflow is available in any method/step being executed by declaring it as a parameter to the method.
Dompap will pass the current workflow if this parameter is included.

````java
public class SomeWorkflow {
    // ...
    public WhenMethod stepA(WorkFlow current) {
        // Get information from current workflow
        passWorkflowInfo(current);        
        return methodWhen(stepB, executionTime);
    }
    
    public Method stepB(WorkFlow current) {
        // At a later time execute something.
        return nextStep;        
    }
}
````

## Recommended practice 

It is recommended to declare members corresponding to each of the steps, to protect from spelling mistakes.

````java
public class SomeWorkflow {
    // ...
    public static Method stepA = method("stepA"); // WorkFlowUtil.getMethod(SomeWorkflow.class, "stepA");
    public Method stepA(WorkFlow current) {
        ...
        return stepB;
    }
    
    public static Method stepB = method("stepB");
    public Method stepB(WorkFlow current) {
        ...        
    }
}
````

# Events

The execution of a workflow is sequential. 
Workflows may receive events at any time and they will be queued up until the workflow flags that execution of these may happen.
The engine will trigger event execution (by calling onEvent) in first-come-first-serve order. 

Usually the workflow will wait for events for a specified time and if nothing was received it will continue:

````java
public class SomeWorkflow {
    // ...
    public WhenMethod callExternalSystem() {
        // Call external system, and expect an event to be sent back
        return methodWhen(noCallbackReceived, timeout, true);
    }
    
    public Method onEvent(WorkFlow<?> currentWorkflow) {
        // Load event
        WorkFlowEvent event = currentWorkflow.loadCurrentEvent();
        // Process event
        // ...        
        return nextStep;
    }
    
    public Method noCallbackReceived() {
        // At a later time execute something.
        return nextStep;        
    }
}
````
  
# Subflows

A workflow may delegate part of its logic to a member. 
This member may hold it's own part of the workflow state and contain several steps to be executed.  
The members state are part of the workflow state so checkpointing will serialize the total state including members.

When the workflow want to leave part of the execution to a member it returns: 
 - a member method to be executed, and
 - a method to be executed

````java
public class SomeWorkflow {
    SubFlow1 flow1;
    SubFlow2 flow2;
    
    public WhenMethod startSubFlow1() {
        return method("flow1", SubFlow1.first).then(startSubFlow2);
    }
    
    public Method startSubFlow2() {
        return method("flow2", SubFlow2.a).then(doMore);
    }

    public Method doMore() { return null; }
}

class SubFlow1 {
    SubState1 state;
    public Method first() { return second; }
    public Method second() { return null; }        
}

class SubFlow2 {
    SubState2 state;
    public Method a() { return a; }
    public Method b() { return null; }        
}
````

# WorkFlow Controller

A workflow may define a custom controller which will act as a kind of entry-exit aspect.
The controller is invoked:
 - prior to executing a method : onStart(WorkFlow<T> workflow, Method methodToBeExecuted)
 - after successful execution of a method: onComplete(WorkFlow<T> workflow, Method methodExecuted, Method nextMethod, Date startWhen)
 - after an exception was thrown during execution of a method: onFail(WorkFlow<T> workflow, Method methodExecuted, Throwable exception)
 
By default the DefaultWorkFlowController is used which handles retrying failed methods, keeping track of number of retries. 

The default is 5 retries with an increasing back off but this is configurable and may even be completely altered
by customizing the WorkFlowController. 

The WorkFlowController keeps state specifically for each workflow instance - i.e. to keep track of number of retries of a specific method etc.  

# Life cycle

The workflows are persistent - until finalized, so when a workflow reaches a "Final State" 
it will be "archived" after a specified number of days (default 30).
Note that failed workflows never completes a "Final State", so they will not be archived until they are resumed 
and completes a "Final State".

## Finalizing workflows

In order for the engine to know when a workflow has completed and can be archived, the FinalState annotation is used.
Further if that method returns null the engine will know that the workflow has completed and will be archived eventually.
When a workflow has completed it will NOT process any events waiting or received.
   
````java
public class SomeWorkflow {
    // ...
    @FinalState
    public Method stepX() {
        // Do whatever
        return null;
    }
}
````


## Archiving 
Thus active database will only contain the "active" workflows and not be impeeded by tons of old completed workflows.
Design-wise this means that a workflow should store any long-term information elsewhere before completing a "Final State". 

Archiving a workflow means that it will be removed from the "active" tables and moved to a separate set a of tables, 
named WFLW_ARCHIVED_... 

The current dashboard/Action List/Domsip REST does not access archived workflows, so these are only available through direct SQL queries.

## Truncation of archive
The intent with the archive is to provide a "last chance" for inspecting month old completed workflows, 
since business may have follow-up questions even after workflow completed.

Eventually even the archive must be removed, which is done through manual truncation. 
This process is manual and requires scripting access to database.

This spreadsheet contains information on latest truncation and resulting table sizes, 
and should be updated accordingly when truncation happens

[/dompap-core/src/main/resources/db/archiving/db-size.xlsx](./dompap-core/src/main/resources/db/archiving/db-size.xlsx)

The scripts for truncation are here:

[/dompap-core/src/main/resources/db/archiving/size-sql.sql](./dompap-core/src/main/resources/db/archiving/size-sql.sql)



 Version
 ====
 Changes in versions
 
 | 5.0 |   | 
 |----|---|
 |  |  |
 




