package demo.solver;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.ortools.sat.*;
import demo.bootstrap.ContextUtil;
import demo.domain.*;
import demo.domain.DTO.OrderIdAndTaskDto;
import demo.service.PhaseOneAssignedTaskService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
@Component
public class PhaseTwoAnother {
    private PhaseOneAssignedTaskService phaseOneAssignedTaskService = ContextUtil.getBean(PhaseOneAssignedTaskService.class);
    private List<Task> taskList;
    private Integer horizon=0;
    Map<String, TaskVariable> allTasks = new HashMap<>();
    Map<String, List<IntervalVar>> resourceToIntervals = new HashMap<>();
    CpModel model = new CpModel();
    private List<ResourceItem> resourceItems;
    List<PhaseTwoAssignedTask> firstAssignedTasks = new ArrayList<>();
    private static int tempTotal = 0;
    private static SubPhaseTwoTask tempTask;

    private Integer calculateHorizon(){
        for (Task task:taskList){
            horizon+= task.getHoursDuration();
        }
        return horizon;
    }

    private void generateVariables() {
        for (Task task : taskList) {
            Integer max = 0;
            if(!task.getIsPublic()){
//                List<Integer> relatedLayer = Arrays.asList(task.getRelatedLayer().
//                        split(",")).stream().mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());
                LambdaQueryWrapper<PhaseOneAssignedTask> wrapper = new LambdaQueryWrapper<>();
//                wrapper.in(PhaseOneAssignedTask::getLayerNum, relatedLayer);
                wrapper.eq(PhaseOneAssignedTask::getOrderId,task.getOrderId());
                List<PhaseOneAssignedTask> phaseOneAssignedTasks = phaseOneAssignedTaskService.list(wrapper);
                max = Collections.max(phaseOneAssignedTasks.stream().map(PhaseOneAssignedTask::getEnd).collect(Collectors.toList()));
            }

            String suffix = "_" + task.getId();
            TaskVariable taskVariable = new TaskVariable();
            taskVariable.setStart(model.newIntVar(max, Integer.MAX_VALUE, "start" + suffix));
            taskVariable.setEnd(model.newIntVar(max, Integer.MAX_VALUE, "end" + suffix));
            taskVariable.setInterval(model.newIntervalVar(taskVariable.getStart(), LinearExpr.constant(task.getHoursDuration())
                    , taskVariable.getEnd(), "interval" + suffix));
            allTasks.put(task.getId(), taskVariable);
            resourceToIntervals.computeIfAbsent(task.getRequiredResourceId(), key -> new ArrayList<>());
            resourceToIntervals.get(task.getRequiredResourceId()).add(taskVariable.getInterval());
        }
        System.out.println("tastlist size"+taskList.size());
    }


    private void createConstraints(){
        System.out.println("resourceIntervalSize"+resourceToIntervals.size());
        for(ResourceItem resourceItem:resourceItems){
            List<IntervalVar> list = resourceToIntervals.get(resourceItem.getId());
            if(list!=null){
                model.addNoOverlap(list);
            }
        }
    }

    //创建按照固定优先级顺序排列的顺序顺序约束
    private void createPrecedence(){
        IntVar minConstant = model.newConstant(1);
        IntVar maxConstant = model.newConstant(3);

        for (Task task : taskList) {
            Task nextTask = task.getNextTask();
            if(nextTask!=null){
                String preKey = task.getId();
                String nextKey = nextTask.getId();
                System.out.println(nextKey);
                model.addGreaterOrEqual(allTasks.get(nextKey).getStart(),allTasks.get(preKey).getEnd());
//                model.addGreaterOrEqual(allTasks.get(nextKey).getStart(), LinearExpr.weightedSum(new IntVar[]{allTasks.get(preKey).getEnd(),minConstant},new long[]{1,1}));
//                model.addLessOrEqual(allTasks.get(nextKey).getStart(), LinearExpr.weightedSum(new IntVar[]{allTasks.get(preKey).getEnd(),maxConstant},new long[]{1,1}));

            }
        }
    }
    private void createPriorityHardConstraint(){
        List<OrderIdAndTaskDto> orderIdAndTaskDtoList = new ArrayList<>();
        Map<String, List<Task>> collect =
                taskList.stream().collect(
                        Collectors.groupingBy(Task::getOrderId,TreeMap::new,
                                Collectors.collectingAndThen(Collectors.toList(),taskList->taskList.stream().sorted(Comparator.comparingInt(Task::getPriority).reversed()).collect(Collectors.toList()))));
        collect.forEach((k,v)->{
            OrderIdAndTaskDto dto = new OrderIdAndTaskDto();
            dto.setOrderId(k);
            dto.setTaskList(v);
            orderIdAndTaskDtoList.add(dto);
        });

        for(int i = 0;i<orderIdAndTaskDtoList.size()-1;i++){
            OrderIdAndTaskDto preDto = orderIdAndTaskDtoList.get(i);
            OrderIdAndTaskDto nextDto = orderIdAndTaskDtoList.get(i+1);
            Task prevTask = preDto.getTaskList().get(0);
            Task nextTask = nextDto.getTaskList().get(0);
            String prevKey = prevTask.getId();
            String nextKey = nextTask.getId();
            model.addLessOrEqual(allTasks.get(prevKey).getStart(),allTasks.get(nextKey).getStart());
        }
    }

    private void defineObjective(){
        IntVar objVar = model.newIntVar(0, Integer.MAX_VALUE, "makespan");
        List<IntVar> ends = new ArrayList<>();
        for (Task task : taskList) {
            Task nextTask = task.getNextTask();
            if(nextTask==null){
                IntVar end = allTasks.get(task.getId()).getEnd();
                ends.add(end);
            }
        }
        model.addMaxEquality(objVar, ends);
        model.minimize(objVar);

    }

    private void solve() {
        CpSolver solver = new CpSolver();
        solver.getParameters().setLogSearchProgress(true);

        CpSolverStatus status = solver.solve(model);
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            System.out.println("Solution:");
            Map<String, List<AssignedTask>> assignedJobs = new HashMap<>();
            for (Task task : taskList) {
                String taskId = task.getId();
                String key = taskId;
                PhaseTwoAssignedTask assignedTask = new PhaseTwoAssignedTask(
                        taskId, (int) solver.value(allTasks.get(key).getStart()), task.getHoursDuration());
                BeanUtils.copyProperties(task,assignedTask);
                assignedJobs.computeIfAbsent(task.getRequiredResourceId(), k -> new ArrayList<>());
                assignedJobs.get(task.getRequiredResourceId()).add(assignedTask);
                firstAssignedTasks.add(assignedTask);
            }

            String output = "";
            for (ResourceItem resourceItem : resourceItems) {
                if(assignedJobs.get(resourceItem.getId())!=null){
                    Collections.sort(assignedJobs.get(resourceItem.getId()), new SortTasks());

                    String solLineTasks = "" + resourceItem.getId() + ": ";
                    String solLine = "           ";
                    for (AssignedTask assignedTask : assignedJobs.get(resourceItem.getId())) {
                        String name = "" + assignedTask.getOriginalId();
                        solLineTasks += String.format("%-15s", name);

                        String solTmp =
                                "[" + assignedTask.getStart() + "," + (assignedTask.getStart() + assignedTask.getHoursDuration()) + "]";
                        // Add spaces to output to align columns.
                        solLine += String.format("%-15s", solTmp);
                    }
                    output += solLineTasks + "%n";
                    output += solLine + "%n";
                }
            }
            System.out.printf("Optimal Schedule Length: %f%n", solver.objectiveValue());
            System.out.printf(output);

        }else{
            System.out.println("No solution found.");
        }
        Collections.sort(firstAssignedTasks, new SortTasks());

    }

    public List<PhaseTwoAssignedTask> solvePhaseTwo(){
        calculateHorizon();
        generateVariables();
        createConstraints();
        createPrecedence();
        defineObjective();
        solve();
        return firstAssignedTasks;
    }


    public static List<SubPhaseTwoTask> splitTask(List<PhaseTwoAssignedTask> tasks){
        List<SubPhaseTwoTask> subTasks = new ArrayList<>();
        for(PhaseTwoAssignedTask phaseOneAssignedTask:tasks){
            tempTotal = 0;
            tempTask = null;
            Integer quantity = phaseOneAssignedTask.getQuantity();
            Integer start = phaseOneAssignedTask.getStart();
            Integer duration = phaseOneAssignedTask.getHoursDuration();
            Integer end = phaseOneAssignedTask.getEnd();
            int remainderStart = start%8;
            int epochStart = start/8;
            int remainderEnd = end%8;
            int epochEnd = end/8;
            int realEnd = 24*epochEnd+remainderEnd+16;
            int realStart = 24*epochStart+remainderStart+16;
            int re = realEnd -realStart;
            int l = (re - duration)/16;
            for(int i =0;i<l+1;i++){
                SubPhaseTwoTask subTask = new SubPhaseTwoTask();
                BeanUtils.copyProperties(phaseOneAssignedTask,subTask);
                subTask.setSubIndex(i);
                int tempStart = 0;
                int tempEnd = 0;
                if(i==0){
                    tempStart = realStart;
                    tempEnd = 24*(epochStart+1);
                    subTask.setStart(tempStart);
                    subTask.setEnd(tempEnd);
                }else if(i==l){
                    tempStart = 24*(epochEnd)+16;
                    tempEnd = realEnd;
                    if(tempStart!=tempEnd){
                        subTask.setStart(tempStart);
                        subTask.setEnd(tempEnd);
                    }
                }else{
                    tempStart = 24*(epochStart+i)+16;
                    tempEnd = 24*(epochStart+i+1);
                    subTask.setStart(tempStart);
                    subTask.setEnd(tempEnd);
                }
                int newDuration = tempEnd - tempStart;
                if(newDuration!=0) {
                    if (tempTotal < quantity) {

                        int newQuantity = (int) (Math.floorDiv(phaseOneAssignedTask.getQuantity() * (tempEnd - tempStart), duration) + 1);
                        tempTotal += newQuantity;
                        subTask.setSubQuantity(newQuantity);
                        subTask.setHoursDuration(newDuration);
                        subTasks.add(subTask);
                        tempTask = subTask;
                    }
                }
            }
            if (tempTotal < quantity) {
                tempTask.setSubQuantity(tempTask.getQuantity() + quantity - tempTotal);
            } else if (tempTotal > quantity) {
                if(quantity - tempTotal + tempTask.getQuantity()>0){
                    tempTask.setSubQuantity(quantity - tempTotal + tempTask.getQuantity());
                }
            }


        }
        subTasks.forEach(i->{
            System.out.println("start:"+i.getStart()+" end: "+i.getEnd()+" substart:"+i.getSubStart()+" subend:"+i.getSubEnd());
        });
        return subTasks;
    }
}
