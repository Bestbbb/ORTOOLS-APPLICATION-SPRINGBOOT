package demo.solver;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
// [START import]

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import demo.bootstrap.ContextUtil;
import demo.bootstrap.DataGenerator;
import demo.bootstrap.DateUtil;
import demo.domain.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jca.context.SpringContextResourceAdapter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.unit.DataUnit;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
// [END import]
@Setter
@Getter
@Service
public class OrToolsJobApp {
    private MesConfig config = ContextUtil.getBean(MesConfig.class);


    Integer horizon = 0;
    Map<String, TaskVariable> allTasks = new HashMap<>();
    Map<String, List<IntervalVar>> resourceToIntervals = new HashMap<>();
    CpModel model = new CpModel();
    private List<Task> taskList;
    private List<ResourceItem> resourceItems;
    private List<ManufacturerOrder> manufacturerOrders;
    LocalDateTime taskBeginTime = LocalDateTime.of(2022, 10, 1, 0, 0, 0);
    List<AssignedTask> firstAssignedTasks = new ArrayList<>();
    List<AssignedTask> finalAssignedTasks = new ArrayList<>();


    private static final LocalTime scheduleOne = LocalTime.of(0, 0, 0);
    private static final LocalTime scheduleTwo = LocalTime.of(8, 0, 0);
    private static final LocalTime scheduleThree = LocalTime.of(16, 0, 0);

    private static final int SCHEDULE_ONE = 0;
    private static final int SCHEDULE_TWO = 1;
    private static final int SCHEDULE_THREE = 2;

    private static int tempTotal = 0;
    private static AssignedTask tempTask;

    private static int index;

    public OrToolsJobApp(){


    }




    public static Schedule generateDemoData() {
        Schedule schedule = new Schedule();
//        List<ResourceItem> resourceItemList = DataGenerator.generateResources();
//        List<ManufacturerOrder> manufacturerOrders = DataGenerator.generateOrderList();
//        List<Task> taskList = DataGenerator.generateTaskList();
//        DataGenerator.sortTask(taskList);
//        List<Allocation> allocationList = DataGenerator.createAllocationList(taskList, manufacturerOrders);
//        schedule.setT        Loader.loadNativeLibraries();askList(taskList);
//        schedule.setAllocationList(allocationList);
//        schedule.setResourceList(resourceItemList);
////        schedule.setResourceRequirementList(null);
//        schedule.setManufacturerOrderList(manufacturerOrders);
        return schedule;
    }
    public Integer calculateHorizon(){
//        List<Task> taskList = DataGenerator.generateTaskList();
        for (Task task:taskList){
            horizon+= task.getHoursDuration();
        }
        return horizon;
    }
    //??????????????????
    public void generateVariables() {
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
        System.out.println(taskList.size());
    }
    //???????????????????????????????????????
    public void createConstraints(){
        System.out.println(resourceToIntervals.size());
        for(ResourceItem resourceItem:resourceItems){
            List<IntervalVar> list = resourceToIntervals.get(resourceItem.getId());
            model.addNoOverlap(list);
        }
    }
    public void createRelatedLayerConstraints(){
        IntVar max = model.newIntVar(0, horizon,"max");
        for (Task task : taskList) {
            Integer unit = task.getUnit();
            if(unit==1){

                List<Integer> relatedLayer = Arrays.asList(task.getRelatedLayer().split(",")).stream().mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());
                if(relatedLayer!=null){
                    String currentTaskId = task.getId();
                    String orderId = task.getOrderId();
                    List<String> processorTaskIds= taskList.stream().filter(task1 -> task1.getLayerNum()!=null && task1.getOrderId().equals(orderId) && relatedLayer.contains(task1.getLayerNum()))
                            .map(Task::getId).collect(Collectors.toList());

                    TaskVariable currentTaskVariable = allTasks.get(currentTaskId);
                    List<IntVar> processorEnds = new ArrayList<>();

                    processorTaskIds.forEach(i->{
                        IntVar end = allTasks.get(i).getEnd();
                        processorEnds.add(end);
                    });
                    model.addMaxEquality(max,processorEnds);
                    model.addGreaterOrEqual(currentTaskVariable.getStart(),max);
                }

            }
        }
    }

    //????????????????????????????????????????????????????????????
    public void createPrecedence(){
        IntVar minConstant = model.newConstant(1);
        IntVar maxConstant = model.newConstant(3);

        for (Task task : taskList) {
            Task nextTask = task.getNextTask();
            if(nextTask!=null){
                Integer unit = task.getUnit();
                Integer nextUnit = nextTask.getUnit();
                if(unit==0&&nextUnit==1){

                }
                String preKey = task.getId();
                String nextKey = nextTask.getId();
                model.addGreaterThan(allTasks.get(nextKey).getStart(),allTasks.get(preKey).getEnd());
//                model.addGreaterOrEqual(allTasks.get(nextKey).getStart(), LinearExpr.weightedSum(new IntVar[]{allTasks.get(preKey).getEnd(),minConstant},new long[]{1,1}));
//                model.addLessOrEqual(allTasks.get(nextKey).getStart(), LinearExpr.weightedSum(new IntVar[]{allTasks.get(preKey).getEnd(),maxConstant},new long[]{1,1}));

            }
        }
    }
    //?????????????????????????????????
    public void defineObjective(){
        IntVar objVar = model.newIntVar(0, horizon, "makespan");
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


    public List<PhaseOneAssignedTask> solvePhaseOne(){
        PhaseOne phaseOne = new PhaseOne();
        //????????????????????????????????????task????????????????????????????????????
        List<Task> beforeIntegratedTaskList = taskList.stream().
                filter(i->i.getUnit()!=null&&i.getUnit()==0&&i.getOrderType()==0&&i.getIsPublic()).collect(Collectors.toList());
        phaseOne.setTaskList(beforeIntegratedTaskList);
        List<Task> beforeIntegratedDemoTaskList = taskList.stream().
                filter(i->i.getUnit()!=null&&i.getUnit()==0&&i.getOrderType()==1&&i.getIsPublic()).collect(Collectors.toList());
        phaseOne.setResourceItems(resourceItems);
        phaseOne.setDemoTaskList(beforeIntegratedDemoTaskList);
        List<PhaseOneAssignedTask> assignedTasks = phaseOne.solvePhaseOne();
        List<PhaseOneAssignedTask> demoAssignedTasks = phaseOne.splitPhaseOne();
        assignedTasks.addAll(demoAssignedTasks);
        return assignedTasks;
    }
    public List<PhaseTwoAssignedTask> solvePhaseTwo(){
        PhaseTwo phaseTwo = new PhaseTwo();
        //???????????????????????????????????????>=??????????????????????????????????????????
        List<Task> afterIntegratedDemoTaskList = taskList.stream().
                filter(i->!i.getIsPublic()&&i.getOrderType()==1).collect(Collectors.toList());
        phaseTwo.setTaskList(afterIntegratedDemoTaskList);
        phaseTwo.setResourceItems(resourceItems);
        List<PhaseTwoAssignedTask> assignedTasks = phaseTwo.solvePhaseTwo();
        return assignedTasks;
    }
    public List<PhaseThreeAssignedTask> solvePhaseThree(){
        PhaseThree phaseThree = new PhaseThree();
        //???????????????????????????????????????>=??????????????????????????????????????????
        List<Task> beforeIntegratedNormalTaskList = taskList.stream().
                filter(i->!i.getIsPublic()&&i.getOrderType()==0&&i.getUnit()==0).collect(Collectors.toList());
//        List<Task> afterIntegratedNormalTaskList = DataGenerator.createAfterIntegratedNormalTaskList(manufacturerOrders);
        phaseThree.setTaskList(beforeIntegratedNormalTaskList);
        phaseThree.setResourceItems(resourceItems);
        List<PhaseThreeAssignedTask> assignedTasks1 = phaseThree.solveThree();

        PhaseThreeLast phaseThreeLast = new PhaseThreeLast();
        //???????????????????????????????????????>=??????????????????????????????????????????
        List<Task> afterIntegratedNormalTaskList = taskList.stream().
                filter(i->!i.getIsPublic()&&i.getOrderType()==0&&i.getUnit()==1).collect(Collectors.toList());
//        List<Task> afterIntegratedNormalTaskList = DataGenerator.createAfterIntegratedNormalTaskList(manufacturerOrders);
        phaseThreeLast.setTaskList(afterIntegratedNormalTaskList);
        phaseThreeLast.setResourceItems(resourceItems);
        phaseThreeLast.setPhaseThreeList(assignedTasks1);
        List<PhaseThreeAssignedTask> assignedTasks2 = phaseThreeLast.solveThree();
        assignedTasks1.addAll(assignedTasks2);
        return assignedTasks1;
    }

    public void connectPhase(){

    }
    //???????????????
    public void solve() {
        CpSolver solver = new CpSolver();
        solver.getParameters().setLogSearchProgress(true);

        CpSolverStatus status = solver.solve(model);
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            System.out.println("Solution:");
            Map<String, List<AssignedTask>> assignedJobs = new HashMap<>();
            for (Task task : taskList) {
                String taskId = task.getId();
                String key = taskId;
                AssignedTask assignedTask = new AssignedTask(
                        taskId, (int) solver.value(allTasks.get(key).getStart()), task.getHoursDuration());
                System.out.println(task.getRelatedLayer());
                BeanUtils.copyProperties(task,assignedTask);
                assignedJobs.computeIfAbsent(task.getRequiredResourceId(), k -> new ArrayList<>());
                assignedJobs.get(task.getRequiredResourceId()).add(assignedTask);
                firstAssignedTasks.add(assignedTask);
            }

            String output = "";
            for (ResourceItem resourceItem : resourceItems) {
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
            System.out.printf("Optimal Schedule Length: %f%n", solver.objectiveValue());
            System.out.printf(output);

        }else{
            System.out.println("No solution found.");
        }
        Collections.sort(firstAssignedTasks, new SortTasks());

    }

    public void output(String algorithmFileId){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime assignedTime = LocalDateTime.now().plusDays(1).with(LocalTime.MIN);
        JSONObject nextWorkDay = DateUtil.getNextWorkDay("");
        String date = nextWorkDay.get("date")+" 00:00:00";
        LocalDateTime assignedTime = LocalDateTime.parse(date,df);

        Output out = new Output();
        out.setCode(200);
        out.setMessage("??????");
        out.setStatus(0);
        out.setRequestId(UUID.randomUUID().toString());

        manufacturerOrders.forEach(i->
        {
            System.out.println("orderId"+i.getId()+"stepSize"+i.getProduct().getStepList().size());
        });
        out.setManufacturerOrderList(manufacturerOrders);
        System.out.println(firstAssignedTasks.size());
        if(firstAssignedTasks.size()==0){

            AssignedTask assignedTask = new AssignedTask();
            assignedTask.setOriginalId("");
            assignedTask.setSubId(0);
            assignedTask.setCode("");
            assignedTask.setSpeed(0);
            assignedTask.setUnit(0);
            assignedTask.setLayerNum(0);
            assignedTask.setTaskType(0);
            assignedTask.setRelatedLayer("");
            assignedTask.setAmount(0);
            assignedTask.setRunTime(LocalDate.now());
            assignedTask.setSchedule(0);
            assignedTask.setStepId("");
            assignedTask.setStart(0);
            assignedTask.setHoursDuration(0);
            assignedTask.setStepIndex(0);
            assignedTask.setOrderIndex(0);
            assignedTask.setQuantity(0);
            for(ManufacturerOrder order:out.getManufacturerOrderList()){
                List<Step> stepList = order.getProduct().getStepList();
                for(Step step:stepList){
                    step.getAssignedTaskList().add(assignedTask);
                }
            }

            System.out.println(JSON.toJSONString(out));
        }else{
        firstAssignedTasks.forEach(assignedTask -> {
            LocalDateTime actualStartTime = assignedTime.plusHours(Optional.of(assignedTask.getStart()).orElse(0));
            LocalDateTime actualEndTime = assignedTime.plusHours(Optional.of(assignedTask.getStart()).orElse(0))
                    .plusHours(Optional.of(assignedTask.getHoursDuration()).orElse(0));
            Duration duration = Duration.between(actualStartTime, actualEndTime);
            long totalMinutes = duration.toMinutes();
            List<LocalDateTime[]> everyDay = getEveryDay(actualStartTime, actualEndTime);
            tempTotal = 0;
            index = 0;
            tempTask = null;
            for(LocalDateTime[] a:everyDay){
                final LocalDateTime tempStartTime = a[0];
                final LocalDateTime tempEndTime = a[1];
                if(tempStartTime.isEqual(tempEndTime)){
                    continue;
                }

                if (tempStartTime.toLocalTime().isBefore(scheduleTwo)) {
                    if (tempEndTime.toLocalTime().isAfter(scheduleThree)) {
                        if (tempTotal < assignedTask.getQuantity()) {
                            setTask(out, assignedTask, tempStartTime, SCHEDULE_ONE, (int) (Math.floorDiv(assignedTask.getQuantity() * Duration.between(tempStartTime.toLocalTime(),
                                    scheduleTwo).toMinutes(), totalMinutes) + 1));
                        }
                        if (tempTotal < assignedTask.getQuantity()) {
                            setTask(out, assignedTask, tempStartTime, SCHEDULE_TWO, (int) (Math.floorDiv(assignedTask.getQuantity() * Duration.between(scheduleTwo,
                                    scheduleThree).toMinutes(), totalMinutes) + 1));
                        }
                        if (tempTotal < assignedTask.getQuantity()) {
                            setTask(out, assignedTask, tempStartTime, SCHEDULE_THREE, (int) (Math.floorDiv(assignedTask.getQuantity() * Duration.between(scheduleThree,
                                    tempEndTime.toLocalTime()).toMinutes(), totalMinutes) + 1));
                        }
                    } else if (tempEndTime.toLocalTime().isAfter(scheduleTwo)) {
                        if (tempTotal < assignedTask.getQuantity()) {
                            setTask(out, assignedTask, tempStartTime, SCHEDULE_ONE, (int) (Math.floorDiv(assignedTask.getQuantity() * Duration.between(tempStartTime.toLocalTime(),
                                    scheduleTwo).toMinutes(), totalMinutes) + 1));
                        }
                        if (tempTotal < assignedTask.getQuantity()) {
                            setTask(out, assignedTask, tempStartTime, SCHEDULE_TWO, (int) (Math.floorDiv(assignedTask.getQuantity() * Duration.between(scheduleTwo,
                                    tempEndTime.toLocalTime()).toMinutes(), totalMinutes) + 1));
                        }
                    } else {
                        if (tempTotal < assignedTask.getQuantity()) {
                            setTask(out, assignedTask, tempStartTime, SCHEDULE_ONE, (int) (Math.floorDiv(assignedTask.getQuantity() * Duration.between(tempStartTime.toLocalTime(),
                                    tempEndTime.toLocalTime()).toMinutes(), totalMinutes) + 1));
                        }
                    }

                } else if (tempStartTime.toLocalTime().isBefore(scheduleThree)) {
                    if (tempEndTime.toLocalTime().isAfter(scheduleThree)) {
                        if (tempTotal < assignedTask.getQuantity()) {

                            setTask(out, assignedTask, tempStartTime, SCHEDULE_TWO, (int) (Math.floorDiv(assignedTask.getQuantity() * Duration.between(tempStartTime.toLocalTime(),
                                    scheduleThree).toMinutes(), totalMinutes) + 1));
                        }
                        if (tempTotal < assignedTask.getQuantity()) {

                            setTask(out, assignedTask, tempStartTime, SCHEDULE_THREE, (int) (Math.floorDiv(assignedTask.getQuantity() * Duration.between(scheduleThree,
                                    tempEndTime.toLocalTime()).toMinutes(), totalMinutes) + 1));
                        }
                    } else {
                        if (tempTotal < assignedTask.getQuantity()) {
                            setTask(out, assignedTask, tempStartTime, SCHEDULE_TWO, (int) (Math.floorDiv(assignedTask.getQuantity() * Duration.between(tempStartTime.toLocalTime(),
                                    tempEndTime.toLocalTime()).toMinutes(), totalMinutes) + 1));
                        }
                    }

                } else {
                    if (tempTotal < assignedTask.getQuantity()) {
                        setTask(out, assignedTask, tempStartTime, SCHEDULE_THREE, (int) (Math.floorDiv(assignedTask.getQuantity() * Duration.between(tempStartTime.toLocalTime(),
                                tempEndTime.toLocalTime()).toMinutes(), totalMinutes) + 1));
                    }
                }
            }
//            System.out.println("id:"+assignedTask.getOriginalId() +"quantity:"+assignedTask.getQuantity()+"tempTotal:"+tempTotal);
            if (tempTotal < assignedTask.getQuantity()) {
                tempTask.setAmount(tempTask.getAmount() + assignedTask.getQuantity() - tempTotal);
            } else if (tempTotal > assignedTask.getQuantity()) {
                if(assignedTask.getQuantity() - tempTotal + tempTask.getAmount()>0){
                    tempTask.setAmount(assignedTask.getQuantity() - tempTotal + tempTask.getAmount());
                }
            }
        });

        for (ManufacturerOrder mOrder : manufacturerOrders) {
            Product product = mOrder.getProduct();
            product.getStepList().forEach(outputStep -> {
                List<AssignedTask> collect = outputStep.getAssignedTaskList().stream().sorted((o1, o2) -> {
                            if (o1.getRunTime().isBefore(o2.getRunTime()))
                                return -1;
                            if (o1.getRunTime().isAfter(o2.getRunTime()))
                                return 1;
                            return 0;
                        }
                ).collect(Collectors.toList());
                finalAssignedTasks.addAll(collect);
            });

        }

        List<AssignedTask> collectFinal = finalAssignedTasks.stream().sorted((o1, o2) -> {
                    if (o1.getRunTime().isBefore(o2.getRunTime()))
                        return -1;
                    if (o1.getRunTime().isAfter(o2.getRunTime()))
                        return 1;
                    return 0;
                }
        ).collect(Collectors.toList());

        DateUtil.setOutputDate(collectFinal);
        DateTimeFormatter df2 = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter df3 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (ManufacturerOrder mOrder : manufacturerOrders) {
            Product product = mOrder.getProduct();
            product.getStepList().forEach(outputStep -> {
                List<AssignedTask> collect = outputStep.getAssignedTaskList().stream().sorted((o1, o2) -> {
                            if (o1.getRunTime().isBefore(o2.getRunTime()))
                                return -1;
                            if (o1.getRunTime().isAfter(o2.getRunTime()))
                                return 1;
                            return 0;
                        }
                ).collect(Collectors.toList());
                LocalDate firstRunTime = collect.get(0).getRunTime();
                outputStep.setStepStartTime(collect.get(0).getRunTime());
                outputStep.setExecutionDays(collect.get(collect.size() - 1).getRunTime().toEpochDay() -
                        collect.get(0).getRunTime().toEpochDay() + 1);
                LocalDate endRunTime = firstRunTime.plusDays(outputStep.getExecutionDays());
                List<String> holidayList = new ArrayList<>();
                for (LocalDate i = firstRunTime; i.isBefore(endRunTime); i = i.plusDays(1)){
                    String dateStr = i.format(df2);
                    Boolean isHoliday = DateUtil.getIsHolidayRoll(dateStr);
                    if (isHoliday){
                        holidayList.add(i.format(df3));
                    }
                    try {
                        Thread.sleep(1000);//???????????????

                    }catch (Exception e){

                    }
                }
                outputStep.setHolidayList(holidayList);

            });

        }
//
//
//        JSONObject json = new JSONObject();
//        JSONArray array = new JSONArray();
//
//        collectFinal.forEach(e -> array.add(e.printInfo()));
//
//        json.put("result", array);

//        DataGenerator.writeResult(json);
        }
        DateTimeFormatter dfDateTime = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        String outputPath = "D:\\data\\"+dfDateTime.format(LocalDateTime.now())+"output.json";
        DataGenerator.writeObjectToFile(out,outputPath);
        sendOutputRequest(outputPath,algorithmFileId);


    }


    private ResponseEntity<String>  sendOutputRequest(String outputPath,String algorithmFileId){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap();
        map.add("outputPath", outputPath);
        map.add("algorithmFileId",algorithmFileId);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(map, headers);
        String url = config.getUrl();
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());
        return response;

    }
    private static void setTask(Output out, AssignedTask task, LocalDateTime runTime, int schedule, Integer amount) {
        if(amount>0) {

            tempTotal += amount;
            AssignedTask assignedTask = new AssignedTask();
            BeanUtils.copyProperties(task, assignedTask);
            assignedTask.setAmount(amount);
            assignedTask.setSubId(index++);
            assignedTask.setRunTime(runTime.toLocalDate());
            assignedTask.setSchedule(schedule);
            tempTask = assignedTask;
            out.getManufacturerOrderList().get(assignedTask.getOrderIndex())
                    .getProduct().getStepList().get(assignedTask.getStepIndex())
                    .getAssignedTaskList().add(assignedTask);
        }
    }

    private static List<LocalDateTime[]> getEveryDay(LocalDateTime actualStartTime, LocalDateTime actualEndTime) {
        List<LocalDateTime[]> result = new ArrayList<>();

        if (actualStartTime.toLocalDate().equals(actualEndTime.toLocalDate())) {
            result.add(new LocalDateTime[]{actualStartTime, actualEndTime});
        } else {
            for (LocalDate i = actualStartTime.toLocalDate(); i.isBefore(actualEndTime.toLocalDate().plusDays(1));
                 i = i.plusDays(1)) {
                if (i.equals(actualStartTime.toLocalDate())) {
                    result.add(new LocalDateTime[]{actualStartTime,
                            LocalDateTime.of(i.getYear(), i.getMonth(), i.getDayOfMonth(), 23, 59, 59)});
                } else if (i.equals(actualEndTime.toLocalDate())) {
                    result.add(new LocalDateTime[]{LocalDateTime.of(i.getYear(), i.getMonth(), i.getDayOfMonth(),
                            0, 0, 0), actualEndTime});
                } else {
                    result.add(new LocalDateTime[]{LocalDateTime.of(i.getYear(), i.getMonth(), i.getDayOfMonth(), 0, 0, 0),
                            LocalDateTime.of(i.getYear(), i.getMonth(), i.getDayOfMonth(), 23, 59, 59)});
                }
            }
        }

        return result;
    }


    public static void main(String[] args) {
//        Loader.loadNativeLibraries();
//
//        OrToolsJobApp orToolsJobApp = new OrToolsJobApp();
//        orToolsJobApp.calculateHorizon();
//        orToolsJobApp.generateVariables();
//        orToolsJobApp.createConstraints();
////        orToolsJobApp.createRelatedLayerConstraints();
//        orToolsJobApp.createPrecedence();
//        orToolsJobApp.defineObjective();
//        orToolsJobApp.solve();
//        orToolsJobApp.output();


    }
}
