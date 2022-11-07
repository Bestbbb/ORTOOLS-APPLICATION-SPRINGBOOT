package demo.bootstrap;


import demo.domain.*;
import demo.jsonUtils.LoadFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataGenerator {
    static String FILE_PATH = "json/input_1104.json";
//    static Input input;
    public final static String OUTPUT_PATH = "D:\\output.json";
    public final static String RESULT_PATH = "D:\\result.json";
//    static {
//        input = LoadFile.readJsonFile(FILE_PATH);
//    }
    public static void writeObjectToFile(Object output,String outputPath) {
        LoadFile.writeJsonFile(output, OUTPUT_PATH);
    }

    public static void writeResult(Object output) {
        LoadFile.writeJsonFile(output, RESULT_PATH);
    }

    public static List<ResourceItem> generateResources(Input input) {
        List<ResourceItem> resourceItemList = new ArrayList<>();
        List<ResourcePool> resourcePool = input.getResourcePool();
        resourcePool.forEach(each -> {
            ResourceItem available = each.getAvailableList().get(0);
            available.setResourcePoolId(each.getId());
            resourceItemList.add(available);

        });
        return resourceItemList;
    }

    public static List<ManufacturerOrder> generateOrderList(Input input) {
        return input.getManufacturerOrderList();
    }



    public static List<Task> generateTaskList(Input input) {
        List<ManufacturerOrder> manufacturerOrderList = input.getManufacturerOrderList();
        List<Task> taskList = new ArrayList<>();
//        ManufacturerOrder order = manufacturerOrderList.get(0);
        int orderIndex=0;
        for (ManufacturerOrder order : manufacturerOrderList) {
            Product product = order.getProduct();
            List<Step> stepList = product.getStepList();
            int stepIndex=0;

            for(Step step:stepList){
                List<ResourceRequirement> resourceRequirementList = step.getResourceRequirementList();
                List<Task> list = step.getTaskList();
                for(Task task :list) {
                    task.setProduct(product);
                    task.setStepIndex(stepIndex);
                    task.setOrderIndex(orderIndex);
                    task.setProductId(product.getId());
                    task.setStepId(step.getId());
                    task.setOrderId(order.getId());
                    task.setQuantity(order.getQuantity());
                    if(order.getType()==1&&order.getRelatedManufactureOrderId()!=null){
                        task.setRelatedOrderId(order.getRelatedManufactureOrderId());
                    }
                    //duration 还得修改
                    task.setDuration((int) Math.ceil((double) order.getQuantity() / task.getSpeed()));
                    task.setSingleTimeSlotSpeed(BigDecimal.valueOf(task.getSpeed()).divide(BigDecimal.valueOf(3), 4, RoundingMode.CEILING));
                    task.setTimeSlotDuration(BigDecimal.valueOf(order.getQuantity()).divide(task.getSingleTimeSlotSpeed(), 4, RoundingMode.CEILING));
                    task.setMinutesDuration((int) Math.ceil(24.0 * 60 * order.getQuantity() / task.getSpeed()));
                    task.setHalfHourDuration((int) Math.ceil(48.0 *order.getQuantity()/task.getSpeed()));
                    task.setHourDuration((int)Math.ceil(24.0*order.getQuantity()/task.getSpeed()));
                    task.setManufacturerOrder(order);
                    task.setRequiredResourceId(resourceRequirementList.get(0).getId());
                }
                taskList.addAll(list);
                stepIndex++;
            }
//            for (int i = stepList.size() - 1; i >= 0; i--) {
//                Step step = stepList.get(i);
//                List<ResourceRequirement> resourceRequirementList = step.getResourceRequirementList();
//                List<Task> stepTaskList = step.getTaskList();
//                Collections.reverse(stepTaskList);
//                int number = i;
//                for (int j = stepTaskList.size() - 1; j >= 0; j--) {
//                    Task item = stepTaskList.get(j);
//                    item.setProduct(product);
//                    item.setProductId(product.getId());
//                    item.setStepId(step.getId());
//                    item.setStepIndex(number);
//                    item.setOrderId(order.getId());
//                    //duration 还得修改
//                    item.setDuration((int) Math.ceil((double) order.getQuantity() / item.getSpeed()));
//                    item.setSingleTimeSlotSpeed(BigDecimal.valueOf(item.getSpeed()).divide(BigDecimal.valueOf(3), 4, RoundingMode.CEILING));
//                    item.setTimeSlotDuration(BigDecimal.valueOf(order.getQuantity()).divide(item.getSingleTimeSlotSpeed(), 4, RoundingMode.CEILING));
//                    item.setMinutesDuration((int) Math.ceil(24.0 * 60 * order.getQuantity() / item.getSpeed()));
//                    item.setManufacturerOrder(order);
//                    item.setRequiredResourceId(resourceRequirementList.get(0).getResourceId());
//                    if (number < stepList.size() - 1) {
//                        Task one = taskList.get(taskList.size() - stepTaskList.size() + j);
//                        item.setNextTask(one);
//                        one.setPreTask(item);
//                    }
//                }
//
//
//                taskList.addAll(stepTaskList);
//            }
//            order.setTaskList(taskList);
            orderIndex++;
        }
        //对每个unit = 0的单个任务设置
        Map<String, Map<Integer, List<Task>>> orderIdToLayerNumberToTasks =
                taskList.parallelStream().filter(task -> task.getLayerNum()!=null).collect(Collectors.groupingBy(Task::getOrderId, Collectors.groupingBy(Task::getLayerNum)));
        orderIdToLayerNumberToTasks.forEach(
                (orderId,map)->{
                    map.forEach((layerNumber,tasks)->{
                        //看是否需要对tasks按照id进行排序
                        for(int i = 0;i<tasks.size();i++){
                            if(i!= tasks.size()-1){
                                Task current = tasks.get(i);
                                Task next = tasks.get(i+1);
                                current.setNextTask(next);
                                next.setPreTask(current);
                            }
                        }
                    });
                }
        );
        //对每个unit=1的套型任务的设置
        Map<String,List<Task>> orderIdToTasks =
                taskList.parallelStream().filter(task -> task.getLayerNum()==null).collect(Collectors.groupingBy(Task::getOrderId));
        orderIdToTasks.forEach((orderId,tasks)->{
            for(int i = 0;i<tasks.size();i++){
                if(i!= tasks.size()-1){
                    Task current = tasks.get(i);
                    Task next = tasks.get(i+1);
                    current.setNextTask(next);
                    next.setPreTask(current);
                }
            }
        });
//        连接relatedlayer和套型的task
        for (Task task : taskList) {
            Integer unit = task.getUnit();
            if(unit==1){
                List<Integer> relatedLayer = task.getRelatedLayer();
                if(relatedLayer!=null){
                    String currentTaskId = task.getId();
                    String orderId = task.getOrderId();
                    Map<Integer,List<Task>> layerNumberToTasks= taskList.parallelStream().
                            filter(task1 -> task1.getLayerNum()!=null && task1.getOrderId().equals(orderId) && relatedLayer.contains(task1.getLayerNum()))
                            .collect(Collectors.groupingBy(Task::getLayerNum));
                    List<List<Task>> taskGroups = layerNumberToTasks.values().stream().collect(Collectors.toList());
                    for(int i=0;i<taskGroups.size();i++){
                        if(i!=taskGroups.size()-1){
                            List<Task> preTasks = taskGroups.get(i);
                            Task preTask = preTasks.get(preTasks.size()-1);
                            List<Task> nextTasks = taskGroups.get(i+1);
                            Task nextTask = nextTasks.get(0);
                            preTask.setNextTask(nextTask);
                            nextTask.setPreTask(preTask);

                        }
                        else{
                            List<Task> preTasks = taskGroups.get(i);
                            Task preTask = preTasks.get(preTasks.size()-1);
                            preTask.setNextTask(task);
                            task.setPreTask(preTask);
                        }
                    }


                }

            }
        }
//        System.out.println(taskList.size());
//        Collections.reverse(taskList);
        taskList.forEach(task ->

        {
            if(task.getNextTask()!=null){
//                System.out.println(task.getHalfHourDuration());
                System.out.println(task.getId() +" next: "+ task.getNextTask().getId()+" Hour duration:"+task.getHourDuration()

                +"Resource:"+task.getRequiredResourceId());
            }
        });
        return taskList;
    }

    public static List<Timeslot> generateTimeSlotList() {
        List<Timeslot> timeslotList = new ArrayList<>(3);
//        timeslotList.add(new Timeslot(0, LocalTime.of(0, 0), LocalTime.of(8, 0)));
//        timeslotList.add(new Timeslot(1, LocalTime.of(8, 0), LocalTime.of(16, 0)));
//        timeslotList.add(new Timeslot(2, LocalTime.of(16, 0), LocalTime.of(24, 0)));
        return timeslotList;
    }





    public static List<ScheduleDate> generateScheduleDateList() {
        List<ScheduleDate> scheduleDateList = new ArrayList<>(14);
        scheduleDateList.add(new ScheduleDate(LocalDateTime.now(), null));

        for (int i = 1; i < 14; i++) {
            scheduleDateList.add(new ScheduleDate(LocalDateTime.now().plusDays(i), null));
        }

        return scheduleDateList;
    }

    public static void main(String[] args) {
//        Integer i = 29;
//        System.out.println(Math.ceil(52.0 / 17));
        Input input = LoadFile.readJsonFile(FILE_PATH);
        List<ManufacturerOrder> manufacturerOrders = DataGenerator.generateOrderList(input);

        List<ResourceItem> resourceItems = DataGenerator.generateResources(input);
        List<Task> tasks = DataGenerator.generateTaskList(input);
        tasks.stream().map(Task::getRelatedLayer).forEach(System.out::println);

//        resourceItems.forEach(i->System.out.println(i.toString()));


    }

    private void reverse(Task task) {

    }

}
