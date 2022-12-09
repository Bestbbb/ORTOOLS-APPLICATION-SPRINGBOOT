package demo.solver;
import java.time.LocalDate;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.ortools.sat.*;
import com.google.ortools.*;
import demo.domain.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Setter
@Getter
public class PhaseOne {

     private List<Task> taskList;
     private List<Task> demoTaskList;
     private Integer horizon=0;
     Map<String, TaskVariable> allTasks = new HashMap<>();
     Map<String, List<IntervalVar>> resourceToIntervals = new HashMap<>();
     CpModel model = new CpModel();
     private List<ResourceItem> resourceItems;
     List<PhaseOneAssignedTask> firstAssignedTasks = new ArrayList<>();

     public List<PhaseOneAssignedTask> splitPhaseOne(){
          //TODO:复杂度太高了，想办法重构吧
          List<PhaseOneAssignedTask> demoAssignedTasks = new ArrayList<>();
          for(PhaseOneAssignedTask assignedTask:firstAssignedTasks){
               if(StringUtils.isNotBlank(assignedTask.getDemoTaskId())){
                    List<String> demoTaskIds = Arrays.asList(assignedTask.getDemoTaskId().split(","));
                    List<Integer> demoTaskQuantity = Arrays.asList(assignedTask.getDemoTaskQuantity().
                           split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
                    Integer hoursDuration = assignedTask.getHoursDuration();
                    Integer end = assignedTask.getEnd();
                    Integer quantity = assignedTask.getQuantity();
                    Integer sumDemoHourDuration = 0;
                    for(int i =0;i<demoTaskIds.size();i++){
                         int demoHoursDuration = (int) Math.ceil((double)hoursDuration / quantity*demoTaskQuantity.get(i));
                         String demoTaskId = demoTaskIds.get(i);
                         PhaseOneAssignedTask phaseOneAssignedTask = new PhaseOneAssignedTask();
                         phaseOneAssignedTask.setOriginalId(demoTaskId);
                         for(Task task:demoTaskList){
                              if(task.getId().equals(demoTaskId)){
                                   BeanUtils.copyProperties(task,phaseOneAssignedTask);
                              }
                         }
                         Integer demoEnd = end-sumDemoHourDuration;
                         phaseOneAssignedTask.setEnd(demoEnd);
                         phaseOneAssignedTask.setHoursDuration(demoHoursDuration);
                         phaseOneAssignedTask.setStart(demoEnd-demoHoursDuration);
                         demoAssignedTasks.add(phaseOneAssignedTask);
                         sumDemoHourDuration+=demoHoursDuration;
                    }
                    Integer actualHoursDuration = hoursDuration-sumDemoHourDuration;
                    Integer actualEnd = end-sumDemoHourDuration;
                    assignedTask.setHoursDuration(actualHoursDuration);
                    assignedTask.setEnd(actualEnd);




               }
          }
          return demoAssignedTasks;

     }

     public List<PhaseOneAssignedTask> solvePhaseOne(){
          calculateHorizon();
          generateVariables();
          createConstraints();
          createPrecedence();
          defineObjective();
          solve();
          return firstAssignedTasks;
     }
     private Integer calculateHorizon(){
          for (Task task:taskList){
               horizon+= task.getHoursDuration();
          }
          return horizon;
     }

     private void generateVariables() {
          for (Task task : taskList) {
               String suffix = "_" + task.getId();
               TaskVariable taskVariable = new TaskVariable();
               taskVariable.setStart(model.newIntVar(0, horizon, "start" + suffix));
               taskVariable.setEnd(model.newIntVar(0, horizon, "end" + suffix));
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
               if(nextTask!=null&&nextTask.getIsPublic()){
                    Integer unit = task.getUnit();
                    Integer nextUnit = nextTask.getUnit();
                    if(unit==0&&nextUnit==1){
                         continue;
                    }
                    String preKey = task.getId();
                    String nextKey = nextTask.getId();
                    System.out.println(nextKey);
                    model.addGreaterOrEqual(allTasks.get(nextKey).getStart(),allTasks.get(preKey).getEnd());
//                model.addGreaterOrEqual(allTasks.get(nextKey).getStart(), LinearExpr.weightedSum(new IntVar[]{allTasks.get(preKey).getEnd(),minConstant},new long[]{1,1}));
//                model.addLessOrEqual(allTasks.get(nextKey).getStart(), LinearExpr.weightedSum(new IntVar[]{allTasks.get(preKey).getEnd(),maxConstant},new long[]{1,1}));

               }
          }
     }

     private void defineObjective(){
          IntVar objVar = model.newIntVar(0, horizon, "makespan");
          List<IntVar> ends = new ArrayList<>();
          for (Task task : taskList) {
               Task nextTask = task.getNextTask();
               if(nextTask==null||!nextTask.getIsPublic()){
                    IntVar end = allTasks.get(task.getId()).getEnd();
                    ends.add(end);
               }
          }
          System.out.println("ends:"+ends.size());
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


}
