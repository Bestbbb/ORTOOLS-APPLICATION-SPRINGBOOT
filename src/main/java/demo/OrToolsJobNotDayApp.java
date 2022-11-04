//package demo;
//
//import com.google.ortools.Loader;
//import com.google.ortools.sat.*;
//import com.google.ortools.util.Domain;
//import demo.bootstrap.DataGenerator;
//import demo.domain.*;
//
//import java.util.*;
//
//public class OrToolsJobNotDayApp {
//    Integer horizon = 0;
//    final Integer maxDays = 3000;
//    final Integer shiftsNumber = 9999;
//    Map<String, TaskVariable> allTasks = new HashMap<>();
//    Map<String, List<IntervalVar>> resourceToIntervals = new HashMap<>();
//    Map<String,Map<Integer,List<IntVar>>> productToLayerNumberToSchedule = new HashMap<>();
//    Map<String, List<IntVar>> resourceToDurations = new HashMap<>();
//    Map<Integer,List<IntVar>> layerNumberToSchedule = new HashMap<>();
//    CpModel model = new CpModel();
//    List<Task> taskList = DataGenerator.generateTaskList();
//    List<ResourceItem> resourceItems = DataGenerator.generateResources();
//    BoolVar[][] shifts;
//    BoolVar[][] sameShift;
//
//    public void createShiftArray(){
//        int taskSize = this.taskList.size();
//        shifts = new BoolVar[taskSize][shiftsNumber];
//        for(int i =0;i<taskSize;i++){
//                for(int j =0;j<shiftsNumber;j++){
//                    shifts[i][j] = model.newBoolVar("taskId_"+taskList.get(i).getId()+"i_"+i+"j_"+j);
//                }
//
//        }
//    }
//
//    public void createSameShift(){
//        int taskSize = this.taskList.size();
//        sameShift = new BoolVar[taskSize][taskSize];
//        for(int i =0;i<taskSize;i++){
//            for(int j =0;j<taskSize;j++){
//                sameShift[i][j] = model.newBoolVar("taskId_i_"+taskList.get(i).getId()+"taskId_j_"+taskList.get(j).getId());
//            }
//        }
//    }
//
//    public static Schedule generateDemoData() {
//        Schedule schedule = new Schedule();
////        List<ResourceItem> resourceItemList = DataGenerator.generateResources();
////        List<ManufacturerOrder> manufacturerOrders = DataGenerator.generateOrderList();
////        List<Task> taskList = DataGenerator.generateTaskList();
////        DataGenerator.sortTask(taskList);
////        List<Allocation> allocationList = DataGenerator.createAllocationList(taskList, manufacturerOrders);
////        schedule.setT        Loader.loadNativeLibraries();askList(taskList);
////        schedule.setAllocationList(allocationList);
////        schedule.setResourceList(resourceItemList);
//////        schedule.setResourceRequirementList(null);
////        schedule.setManufacturerOrderList(manufacturerOrders);
//        return schedule;
//    }
//    public Integer calculateHorizon(){
//        List<Task> taskList = DataGenerator.generateTaskList();
//        for (Task task:taskList){
//            horizon+= task.getMinutesDuration();
//        }
//        return horizon;
//    }
//    //生成计划变量
//    public void generateVariables() {
//        for (Task task : taskList) {
//            String suffix = "_" + task.getId();
//            TaskVariable taskVariable = new TaskVariable();
//            IntVar scheduleRange = model.newIntVarFromDomain(Domain.allValues(),"schedule_"+suffix);
//            taskVariable.setSchedule(scheduleRange);
//            taskVariable.setStart(model.newIntVar(0, horizon, "start" + suffix));
//            taskVariable.setDuration(model.newIntVar(0,task.getMinutesDuration(),"duration"+suffix));
//            taskVariable.setEnd(model.newIntVar(0, horizon, "end" + suffix));
//            taskVariable.setInterval(model.newIntervalVar(taskVariable.getStart(), LinearExpr.constant(task.getMinutesDuration())
//                    , taskVariable.getEnd(), "interval" + suffix));
//            IntVar timeslotStart = model.newIntVarFromDomain(Domain.fromFlatIntervals(new long[]{0L,480L,960L}),"Timeslot_start"+suffix);
//            taskVariable.setTimeslotInterval(
//                    model.newFixedSizeIntervalVar(timeslotStart,480,"timeslot_"+suffix));
//            allTasks.put(task.getId(), taskVariable);
//            Integer layerNumber = task.getLayerNum();
//            if(layerNumber!=null){
//                layerNumberToSchedule.computeIfAbsent(task.getLayerNum(),key->new ArrayList<>());
//                layerNumberToSchedule.get(task.getLayerNum()).add(taskVariable.getSchedule());
//            }
//            resourceToIntervals.computeIfAbsent(task.getRequiredResourceId(), key -> new ArrayList<>());
//            resourceToIntervals.get(task.getRequiredResourceId()).add(taskVariable.getInterval());
//        }
//    }
//    //创建时间间隔不能重复的约束
//    public void createConstraints(){
//        for(ResourceItem resourceItem:resourceItems){
//            List<IntervalVar> list = resourceToIntervals.get(resourceItem.getResourcePoolId());
//            model.addNoOverlap(list);
//        }
//    }
//
//    //创建同一层的不同工艺尽量不要在同一个班次的约束
//    public void createSameLayerSeriesNotInTimeSlotConstraints(){
//        for (Task task : taskList) {
//            Integer layerNumber = task.getLayerNum();
//            if(layerNumber!=null) {
//                List<IntVar> list = layerNumberToSchedule.get(layerNumber);
//                model.addAllDifferent(list);
//            }
//        }
//    }
//    //创建排在同一班次的资源尽可能被满占用的约束
//    public void fullCoveredConstraints(){
//        IntVar maxMinutesDuration = model.newConstant(480);
//        for(int i=0;i<taskList.size();i++){
//            for(int j =0;j<shiftsNumber;j++){
//                LinearExpr sumDuration =
//                        LinearExpr.weightedSum(new BoolVar[] {shifts[i][j]}, new long[] {taskList.get(i).getMinutesDuration()});
//                model.addLessOrEqual(sumDuration,maxMinutesDuration);
//                }
//        }
//    }
//
//    //创建排在同一班次的资源尽可能被满占用的约束
//    public void fullCoveredConstraints2(){
//        IntVar maxMinutesDuration = model.newConstant(480);
//        for(int i=0;i<taskList.size();i++) {
//            for (int j = 0; j < taskList.size(); j++) {
//                if(i!=j) {
//                    LinearExpr sumDuration =
//                            LinearExpr.weightedSum(new BoolVar[]{sameShift[i][j]}, new long[]{taskList.get(i).getMinutesDuration()});
//                    model.addLessOrEqual(sumDuration, maxMinutesDuration);
//                    model.addEquality(allTasks.get(taskList.get(i).getId()).getSchedule(),allTasks.get(taskList.get(j).getId()).getSchedule())
//                            .onlyEnforceIf(sameShift[i][j]);
//                }
//            }
//        }
//    }
//
//    //创建优先级高的订单的完成时间要早于优先级低的完成时间的约束
//    public void createPriorityEndTimeConstraint(){
//
//    }
//    //创建叠层相关约束
//    public void createRelatedLayerConstraints(){
//        for (Task task : taskList) {
//            if(task.getUnit()==1){
//                List<Integer> relatedLayer = task.getRelatedLayer();
//
//            }
//        }
//    }
//    //创建按照固定优先级顺序排列的顺序的约束
//    public void createPrecedence(){
//        for (Task task : taskList) {
//            Task nextTask = task.getNextTask();
//            if(nextTask!=null){
//                String preKey = task.getId();
//                String nextKey = nextTask.getId();
//                model.addGreaterThan(allTasks.get(nextKey).getSchedule(), allTasks.get(preKey).getSchedule());
//            }
//        }
//    }
//    //定义目标函数和训练规则
//    public void defineObjective() {
////        IntVar objVar = model.newIntVar(0, horizon, "makespan");
//        List<LinearArgument> ends = new ArrayList<>();
////        for (Task task : taskList) {
////            Task nextTask = task.getNextTask();
////            if (nextTask == null) {
////                IntVar end = allTasks.get(task.getId()).getSchedule();
////                ends.add(end);
////            }
////        }
//        for (int i = 0; i < taskList.size(); i++) {
//            for (int j = 0; j < taskList.size(); j++) {
//                if (i != j) {
//                    LinearExpr sumDuration =
//                            LinearExpr.weightedSum(new BoolVar[]{sameShift[i][j]}, new long[]{taskList.get(j).getMinutesDuration()});
//                    ends.add(sumDuration);
//
//                }
//            }
//            LinearArgument[] array3 = ends.toArray(new LinearArgument[ends.size()]);
//
//            model.minimize(LinearExpr.sum(array3));
//
//        }
//    }
//
//    //自定义求解器配置
//    public void solve2() {
//        CpSolver solver = new CpSolver();
//        CpSolverStatus status = solver.solve(model);
//        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
//            for(int i=0;i<taskList.size();i++){
//                boolean isWorking = false;
//                for(int j =0;j<shiftsNumber;j++) {
//                    if (solver.booleanValue(shifts[i][j])) {
//                        isWorking = true;
//                        System.out.printf("  Nurse %d work shift %d%n", i, j);
//
//                    }
//
//                    }
//                if (!isWorking) {
//                    System.out.printf("  Nurse %d does not work%n", i);
//                }
//                }
//        }
//    }
//    //求解器配置
//    public void solve() {
//        CpSolver solver = new CpSolver();
//        CpSolverStatus status = solver.solve(model);
//        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
//            System.out.println("Solution:");
//            Map<String, List<AssignedTask>> assignedJobs = new HashMap<>();
//            for (Task task : taskList) {
//                String taskId = task.getId();
//                String key = taskId;
////                System.out.println(key);
//                AssignedTask assignedTask = new AssignedTask(
//                        taskId,(int) solver.value(allTasks.get(key).getSchedule()),0);
//                assignedJobs.computeIfAbsent(task.getRequiredResourceId(), k -> new ArrayList<>());
//                assignedJobs.get(task.getRequiredResourceId()).add(assignedTask);
//            }
//
//            String output = "";
//            for (ResourceItem resourceItem : resourceItems) {
//                Collections.sort(assignedJobs.get(resourceItem.getResourcePoolId()), new SortTasks());
//                String solLineTasks = "" + resourceItem.getResourcePoolId() + ": ";
//                String solLine = "           ";
//                for (AssignedTask assignedTask : assignedJobs.get(resourceItem.getResourcePoolId())) {
//                    String name = "" + assignedTask.getOriginalId();
//                    solLineTasks += String.format("%-15s", name);
//
//                    String solTmp =
//                            "[" + assignedTask.getSchedule() + "," + (assignedTask.getSchedule()) + "]";
//                    // Add spaces to output to align columns.
//                    solLine += String.format("%-15s", solTmp);
//                }
//                output += solLineTasks + "%n";
//                output += solLine + "%n";
//            }
//            System.out.printf("Optimal Schedule Length: %f%n", solver.objectiveValue());
//            System.out.printf(output);
//
//        }else{
//            System.out.println("No solution found.");
//        }
//    }
//
//
//    public static void main(String[] args) {
//        Loader.loadNativeLibraries();
//
//        OrToolsJobNotDayApp orToolsJobApp = new OrToolsJobNotDayApp();
//        orToolsJobApp.createShiftArray();
//
////        orToolsJobApp.createSameShift();
//        orToolsJobApp.calculateHorizon();
////        orToolsJobApp.generateVariables();
//        orToolsJobApp.fullCoveredConstraints();
////        orToolsJobApp.createSameLayerSeriesNotInTimeSlotConstraints();
//        orToolsJobApp.defineObjective();
//        orToolsJobApp.solve2();
//
//
//    }
//}
