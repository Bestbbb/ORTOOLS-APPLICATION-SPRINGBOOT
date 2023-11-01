package demo.solver;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.ortools.sat.*;
import demo.domain.*;
import demo.domain.DTO.OrderIdAndTaskDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
public class PhaseUnit1 {

    private List<Task> taskList;
    private List<PhaseOneAssignedTask> phaseOneAssignedTasks;

    private List<Task> demoTaskList;
    private Integer horizon = 0;
    Map<String, TaskVariable> allTasks = new HashMap<>();
    Map<String, List<TaskVariable>> resourceToIntervals = new HashMap<>();
    CpModel model = new CpModel();

    private List<ResourceItem> resourceItems;
    List<PhaseOneAssignedTask> firstAssignedTasks = new ArrayList<>();
    private static int tempTotal = 0;
    private static SubPhaseOneTask tempTask;

    private Set<Integer> set = new HashSet<>();


    public List<PhaseOneAssignedTask> splitPhaseOne() {
        //TODO:复杂度太高了，想办法重构吧
        List<PhaseOneAssignedTask> demoAssignedTasks = new ArrayList<>();
        for (PhaseOneAssignedTask assignedTask : firstAssignedTasks) {
            if (StringUtils.isNotBlank(assignedTask.getDemoTaskId())) {
                List<String> demoTaskIds = Arrays.asList(assignedTask.getDemoTaskId().split(","));
                List<Integer> demoTaskQuantity = Arrays.asList(assignedTask.getDemoTaskQuantity().
                        split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
                List<Integer> demoTaskDuration = Arrays.asList(assignedTask.getDemoTaskDuration().
                        split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
                Integer sum = demoTaskQuantity.stream().reduce(Integer::sum).orElse(0);
//                    Integer demoTaskDurationSum = demoTaskDuration.stream().reduce(Integer::sum).orElse(0);
                Integer hoursDuration = assignedTask.getHoursDuration();
                Integer end = assignedTask.getEnd();
                Integer start = assignedTask.getStart();
                Integer sumDemoHourDuration = 0;
                for (int i = 0; i < demoTaskIds.size(); i++) {
                    int demoHoursDuration = demoTaskDuration.get(i);
//                         int demoHoursDuration= (Math.floorDiv(hoursDuration,quantity*demoTaskQuantity.get(i)) + 1);
//                         int demoHoursDuration = (int) Math.ceil((double)hoursDuration / quantity*demoTaskQuantity.get(i));
                    String demoTaskId = demoTaskIds.get(i);
                    PhaseOneAssignedTask phaseOneAssignedTask = new PhaseOneAssignedTask();
                    phaseOneAssignedTask.setOriginalId(demoTaskId);
                    for (Task task : demoTaskList) {
                        if (task.getId().equals(demoTaskId)) {
                            BeanUtils.copyProperties(task, phaseOneAssignedTask);
                        }
                    }
//                         int demoHoursDuration= (Math.floorDiv(24* demoTaskQuantity.get(i), phaseOneAssignedTask.getSpeed()) + 1);
//                         int demoHoursDuration = (int) Math.ceil(24.0* demoTaskQuantity.get(i) / phaseOneAssignedTask.getSpeed());
//                         Integer demoEnd = end-sumDemoHourDuration;
                    Integer demoStart = start + sumDemoHourDuration;

                    phaseOneAssignedTask.setEnd(demoStart + demoHoursDuration);
                    phaseOneAssignedTask.setHoursDuration(demoHoursDuration);
                    phaseOneAssignedTask.setStart(demoStart);
                    demoAssignedTasks.add(phaseOneAssignedTask);
                    sumDemoHourDuration += demoHoursDuration;
                }
                Integer actualHoursDuration = hoursDuration - sumDemoHourDuration;
                if (actualHoursDuration != 0) {
//                         Integer actualEnd = end - sumDemoHourDuration;
                    assignedTask.setHoursDuration(actualHoursDuration);
//                         assignedTask.setEnd(actualEnd);
                    Integer actualStart = start + sumDemoHourDuration;
                    assignedTask.setStart(actualStart);

                }


            }
        }
        return demoAssignedTasks;

    }

    public List<PhaseOneAssignedTask> solvePhaseOne() {
        calculateHorizon();
        generateVariables();
//        addNoOverlapManual();
//          createConstraints();
        createPrecedence();
//         createPriorityHardConstraint();
//          createPriorityConstraint();
        defineObjective();
        solve();
        return firstAssignedTasks;
    }

    private Integer calculateHorizon() {
        for (Task task : taskList) {
            horizon += task.getHoursDuration();
        }
        return horizon;

    }

    private void generateVariables() {

        for (Task task : taskList) {
            Integer maxEnd = 0;

                    List<Integer> collect = phaseOneAssignedTasks.stream().filter(i->i.getOrderId().equals(task.getOrderId())|| i.getOrderId().equals(task.getRelatedOrderId()))
                            .map(PhaseOneAssignedTask::getEnd).collect(Collectors.toList());
                    maxEnd = Collections.max(collect);
                    if(maxEnd!=null){
                         set.add(maxEnd);

                    }


            String suffix = "_" + task.getId();
            TaskVariable taskVariable = new TaskVariable();
            taskVariable.setStart(model.newIntVar(maxEnd, maxEnd + horizon, "start" + suffix));
            taskVariable.setEnd(model.newIntVar(maxEnd, maxEnd + horizon, "end" + suffix));
            if (allTasks.get(task.getId()) != null) {
                System.out.println(task.getId() + "nmsl2");
            }
            taskVariable.setInterval(model.newIntervalVar(taskVariable.getStart(), LinearExpr.constant(task.getHoursDuration())
                    , taskVariable.getEnd(), "interval" + suffix));
            allTasks.put(task.getId(), taskVariable);
            resourceToIntervals.computeIfAbsent(task.getRequiredResourceId(), key -> new ArrayList<>());
            resourceToIntervals.get(task.getRequiredResourceId()).add(taskVariable);
        }
        System.out.println("tastlist size" + taskList.size());
    }

    private void addNoOverlapManual() {
        System.out.println(resourceToIntervals.keySet());
        for (ResourceItem resourceItem : resourceItems) {
            System.out.println(resourceItem.getId());
            List<TaskVariable> intervals = resourceToIntervals.get(resourceItem.getId());
            if (intervals != null) {
                List<IntervalVar> intervalVars = new ArrayList<>();
                for(TaskVariable taskVariable:intervals)
                {
                    intervalVars.add(taskVariable.getInterval());

                }
                model.addNoOverlap(intervalVars);

            }

        }


    }


//     private void createConstraints(){
//          System.out.println("resourceIntervalSize"+resourceToIntervals.size());
//          for(ResourceItem resourceItem:resourceItems){
//               List<IntervalVar> list = resourceToIntervals.get(resourceItem.getId());
//               if(list!=null){
//                    model.addNoOverlap(list);
//               }
//          }
//     }

    //创建按照固定优先级顺序排列的顺序顺序约束
    private void createPrecedence() {
        for (Task task : taskList) {
            Task nextTask = task.getNextTask();
            if (nextTask != null) {
                String preKey = task.getId();
                String nextKey = nextTask.getId();
                System.out.println(nextKey);
                model.addGreaterOrEqual(allTasks.get(nextKey).getStart(), allTasks.get(preKey).getEnd());
//                model.addGreaterOrEqual(allTasks.get(nextKey).getStart(), LinearExpr.weightedSum(new IntVar[]{allTasks.get(preKey).getEnd(),minConstant},new long[]{1,1}));
//                model.addLessOrEqual(allTasks.get(nextKey).getStart(), LinearExpr.weightedSum(new IntVar[]{allTasks.get(preKey).getEnd(),maxConstant},new long[]{1,1}));

            }
        }
    }

    private void createPriorityHardConstraint() {
        List<OrderIdAndTaskDto> orderIdAndTaskDtoList = new ArrayList<>();
        Map<String, List<Task>> collect =
                taskList.stream().collect(
                        Collectors.groupingBy(Task::getOrderId, TreeMap::new,
                                Collectors.collectingAndThen(Collectors.toList(), taskList -> taskList.stream().sorted(Comparator.comparingInt(Task::getPriority).reversed()).collect(Collectors.toList()))));
        collect.forEach((k, v) -> {
            OrderIdAndTaskDto dto = new OrderIdAndTaskDto();
            dto.setOrderId(k);
            dto.setTaskList(v);
            orderIdAndTaskDtoList.add(dto);
        });

        for (int i = 0; i < orderIdAndTaskDtoList.size() - 1; i++) {
            OrderIdAndTaskDto preDto = orderIdAndTaskDtoList.get(i);
            OrderIdAndTaskDto nextDto = orderIdAndTaskDtoList.get(i + 1);
            Task prevTask = preDto.getTaskList().get(0);
            Task nextTask = nextDto.getTaskList().get(0);
            String prevKey = prevTask.getId();
            String nextKey = nextTask.getId();
            model.addLessOrEqual(allTasks.get(prevKey).getStart(), allTasks.get(nextKey).getStart());
        }
    }

    private void createPriorityConstraint() {

        List<Task> collect = taskList.stream().sorted((o1, o2) -> {
            if (o1.getPriority() > o2.getPriority())
                return -1;
            if (o1.getPriority() < o2.getPriority())
                return 1;
            return 0;

        }).collect(Collectors.toList());
        collect.forEach(i -> System.out.println(i.getId() + " " + i.getPriority()));
        List<BoolVar> boolVars = new ArrayList<>();
        for (int i = 0; i < collect.size() - 1; i++) {
            Task preTask = collect.get(i);
            Task nextTask = collect.get(i + 1);
            String prevKey = preTask.getId();
            String nextKey = nextTask.getId();
//      model.addGreaterOrEqual(allTasks.get(prevKey).start,allTasks.get(nextKey).start);
            BoolVar boolVar = model.newBoolVar("xianhou");
            model.addLessOrEqual(allTasks.get(prevKey).getStart(), allTasks.get(nextKey).getStart()).onlyEnforceIf(boolVar);
            model.addGreaterOrEqual(allTasks.get(prevKey).getStart(), allTasks.get(nextKey).getStart()).onlyEnforceIf(boolVar.not());
            boolVars.add(boolVar);
//      model.addCircuit().addArc(i,i+1,boolVar);

        }
        System.out.println(boolVars.size());
        BoolVar[] boolVarss = new BoolVar[boolVars.size()];
        BoolVar[] boolVars1 = boolVars.toArray(boolVarss);
        model.maximize(LinearExpr.sum(boolVars1));
    }

    private void defineObjective() {
          Integer max = set.stream().max(Integer::compareTo).orElse(null);
          // 直接获得最小值
          Integer min = set.stream().min(Integer::compareTo).orElse(null);
        IntVar objVar = model.newIntVar(min, max+horizon, "makespan");
        List<IntVar> ends = new ArrayList<>();
        for (Task task : taskList) {
            Task nextTask = task.getNextTask();
            if (nextTask == null) {
                IntVar end = allTasks.get(task.getId()).getEnd();
                ends.add(end);
            }
        }
        System.out.println("ends:" + ends.size());
        model.addMaxEquality(objVar, ends);
        model.minimize(objVar);

    }

    private void solve() {
        CpSolver solver = new CpSolver();

        solver.getParameters().setLogSearchProgress(true);

        CpSolverStatus status = solver.solve(model);
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            System.out.println("Solution:");
            Map<String, List<PhaseOneAssignedTask>> assignedJobs = new HashMap<>();
            for (Task task : taskList) {
                String taskId = task.getId();
                String key = taskId;
                PhaseOneAssignedTask assignedTask = new PhaseOneAssignedTask(
                        taskId, (int) solver.value(allTasks.get(key).getStart()), task.getHoursDuration());
                BeanUtils.copyProperties(task, assignedTask);
                assignedJobs.computeIfAbsent(task.getRequiredResourceId(), k -> new ArrayList<>());
                assignedJobs.get(task.getRequiredResourceId()).add(assignedTask);
                firstAssignedTasks.add(assignedTask);
            }

            String output = "";
            for (ResourceItem resourceItem : resourceItems) {
                if (assignedJobs.get(resourceItem.getId()) != null) {
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

        } else {
            System.out.println("Phase one last:No solution found.");
        }
        Collections.sort(firstAssignedTasks, new SortTasks());

    }

    public static List<SubPhaseOneTask> splitTask(List<PhaseOneAssignedTask> tasks) {
        List<SubPhaseOneTask> subTasks = new ArrayList<>();
        for (PhaseOneAssignedTask phaseOneAssignedTask : tasks) {
            tempTotal = 0;
            tempTask = null;
            Integer quantity = phaseOneAssignedTask.getQuantity();
            Integer start = phaseOneAssignedTask.getStart();
            Integer duration = phaseOneAssignedTask.getHoursDuration();
            Integer end = phaseOneAssignedTask.getEnd();
            int remainderStart = start % 16;
            int epochStart = start / 16;
            int remainderEnd = end % 16;
            int epochEnd = end / 16;
            int realEnd = 24 * epochEnd + remainderEnd;
            int realStart = 24 * epochStart + remainderStart;
            int re = realEnd - realStart;
            int l = (re - duration) / 8;
            for (int i = 0; i < l + 1; i++) {

                SubPhaseOneTask subTask = new SubPhaseOneTask();
                BeanUtils.copyProperties(phaseOneAssignedTask, subTask);
                subTask.setSubIndex(i);
                int tempStart = 0;
                int tempEnd = 0;
                if (i == 0) {
                    tempStart = realStart;
                    tempEnd = 24 * epochStart + 16;
                    subTask.setStart(tempStart);
                    subTask.setEnd(tempEnd);

                } else if (i == l) {
                    tempStart = 24 * (epochEnd);
                    tempEnd = realEnd;
                    if (tempStart != tempEnd) {
                        subTask.setStart(tempStart);
                        subTask.setEnd(tempEnd);
                    }
                } else {
                    tempStart = 24 * (epochStart + i);
                    tempEnd = 24 * (epochStart + i) + 16;
                    subTask.setStart(tempStart);
                    subTask.setEnd(tempEnd);
                }
                int newDuration = tempEnd - tempStart;

                if (newDuration != 0) {
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
                tempTask.setSubQuantity(tempTask.getSubQuantity() + quantity - tempTotal);
            } else if (tempTotal > quantity) {
                if (quantity - tempTotal + tempTask.getSubQuantity() > 0) {
                    tempTask.setSubQuantity(quantity - tempTotal + tempTask.getSubQuantity());
                }
            }


        }
//          subTasks.forEach(i->{
//               System.out.println("id:"+i.getOriginalId()+" start:"+i.getStart()+" end: "+i.getEnd()+" duration:"+i.getHoursDuration()+" quantity:"+i.getQuantity());
//          });
        return subTasks;
    }


}
