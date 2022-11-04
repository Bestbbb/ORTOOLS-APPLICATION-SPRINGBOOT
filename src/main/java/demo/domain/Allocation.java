package demo.domain;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class Allocation{

    private String id;

    private Task task;

    private Allocation sourceAllocation;
    private Allocation sinkAllocation;
    private List<Allocation> predecessorAllocationList;
    private List<Allocation> successorAllocationList;

    // Planning variables: changes during planning, between score calculations.
//    private ExecutionMode executionMode;
    private Integer delay; // In days

    // Shadow variables
    private Integer predecessorsDoneDate;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Allocation getSourceAllocation() {
        return sourceAllocation;
    }

    public void setSourceAllocation(Allocation sourceAllocation) {
        this.sourceAllocation = sourceAllocation;
    }

    public Allocation getSinkAllocation() {
        return sinkAllocation;
    }

    public void setSinkAllocation(Allocation sinkAllocation) {
        this.sinkAllocation = sinkAllocation;
    }

    public List<Allocation> getPredecessorAllocationList() {
        return predecessorAllocationList;
    }

    public void setPredecessorAllocationList(List<Allocation> predecessorAllocationList) {
        this.predecessorAllocationList = predecessorAllocationList;
    }

    public List<Allocation> getSuccessorAllocationList() {
        return successorAllocationList;
    }

    public void setSuccessorAllocationList(List<Allocation> successorAllocationList) {
        this.successorAllocationList = successorAllocationList;
    }

//    @PlanningVariable(valueRangeProviderRefs = {
//            "executionModeRange" }, strengthWeightFactoryClass = ExecutionModeStrengthWeightFactory.class)
//    public ExecutionMode getExecutionMode() {
//        return executionMode;
//    }
//
//    public void setExecutionMode(ExecutionMode executionMode) {
//        this.executionMode = executionMode;
//    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Integer getPredecessorsDoneDate() {
        return predecessorsDoneDate;
    }

    public void setPredecessorsDoneDate(Integer predecessorsDoneDate) {
        this.predecessorsDoneDate = predecessorsDoneDate;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public Integer getStartDate() {
        if (predecessorsDoneDate == null) {
            return null;
        }
        return predecessorsDoneDate + (delay == null ? 0 : delay);
    }

    public Integer getEndDate(int amount) {
        if (predecessorsDoneDate == null) {
            return null;
        }
        return predecessorsDoneDate + (delay == null ? 0 : delay) +(int) Math.ceil((double)amount / this.getTask().getSpeed());
    }

    public ManufacturerOrder getManufacturerOrder() {
        return task.getManufacturerOrder();
    }

//    public int getProjectCriticalPathEndDate() {
//        return job.getProject().getCriticalPathEndDate();
//    }

//    public JobType getJobType() {
//        return job.getJobType();
//    }

    public String getLabel() {
        return "Job " + task.getId();
    }

    // ************************************************************************
    // Ranges
    // ************************************************************************

//    @ValueRangeProvider(id = "executionModeRange")
//    public List<ExecutionMode> getExecutionModeRange() {
//        return job.getExecutionModeList();
//    }
//
//    @ValueRangeProvider(id = "delayRange")
//    public CountableValueRange<Integer> getDelayRange() {
//        return ValueRangeFactory.createIntValueRange(1, 20);
//    }


    private Integer stepIndex;

    public void setStepIndex(int stepIndex){
        this.stepIndex=stepIndex;
    }
    public Integer getStepIndex(){
        return stepIndex;
    }

    private final LocalDateTime actualStartedTime = LocalDateTime.of(2022,10,1,0,0,0);

    public LocalDateTime getActualStartTime() { return actualStartedTime.plusDays(Optional.ofNullable(getStartDate()).orElse(0)); }

    public LocalDateTime getActualEndTime() { return actualStartedTime.plusDays(Optional.ofNullable(getEndDate(150)).orElse(0)); }


}
