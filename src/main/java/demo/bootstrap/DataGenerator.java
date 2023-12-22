package demo.bootstrap;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.common.base.Joiner;
import com.sun.jna.WString;
import demo.domain.*;
import demo.jsonUtils.LoadFile;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.ls.LSException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataGenerator {
    static String FILE_PATH = "D:\\文档\\Idea Projects\\ORTOOLS-APPLICATION\\src\\main\\resources\\json\\2022-12-28.json";
    public final static String OUTPUT_PATH = "D:\\output.json";
    public final static String RESULT_PATH = "D:\\result.json";

    public static void writeObjectToFile(Object output, String outputPath) {
        LoadFile.writeJsonFile(output, outputPath);
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
        List<ManufacturerOrder> manufacturerOrderList = input.getManufacturerOrderList();

        // 把一个订单才分2个小定单
        for (int i = 0; i < manufacturerOrderList.size(); i++) {
            ManufacturerOrder order = manufacturerOrderList.get(i);
            order.setIndex(i);
            order.setJoinQuantity(order.getQuantity());
        }
        //合并小样单和正常单
//        joinOrderList(manufacturerOrderList);
//        List<List<ManufacturerOrder>> lists = divideOrderToTwoPart(manufacturerOrderList);

//        for(List<ManufacturerOrder> i:lists){
//            for(ManufacturerOrder manufacturerOrder:i){
//                System.out.println("orderId"+manufacturerOrder.getId());
//            }
//        };
//        manufacturerOrderList.forEach(
//                i->i.getProduct().getStepList().forEach(j->{
//                    for (Task task : j.getTaskList()) {
//                        System.out.println(task.getOrderId()+" " +task.getSpeed());
//                    }
//                })
//        );
        List<ManufacturerOrder> collect = manufacturerOrderList.stream().sorted(
                (o1, o2) -> {
                    if (o1.getPriority() > o2.getPriority())
                        return -1;
                    if (o1.getPriority() < o2.getPriority())
                        return 1;
                    return 0;
                }
        ).collect(Collectors.toList());
        collect.forEach(i -> System.out.println("prioirty" + i.getPriority()));
        return manufacturerOrderList;
    }

    public static List<List<ManufacturerOrder>> generateOrderListNew(List<ManufacturerOrder> manufacturerOrderList) {

        // 把一个订单才分2个小定单

        //合并小样单和正常单
        List<List<ManufacturerOrder>> lists = divideOrderToTwoPart(manufacturerOrderList);
//        for(List<ManufacturerOrder> i:lists){
//            for(ManufacturerOrder manufacturerOrder:i){
//                System.out.println("orderId"+manufacturerOrder.getId());
//            }
//        };
//        manufacturerOrderList.forEach(
//                i->i.getProduct().getStepList().forEach(j->{
//                    for (Task task : j.getTaskList()) {
//                        System.out.println(task.getOrderId()+" " +task.getSpeed());
//                    }
//                })
//        );

        return lists;
    }


    private static List<List<ManufacturerOrder>> divideOrderToTwoPart(List<ManufacturerOrder> manufacturerOrderList) {
        HashMap<String, List<ManufacturerOrder>> dict = new HashMap<>();
        List<ManufacturerOrder> collect = manufacturerOrderList.stream().sorted(
                (o1, o2) -> {
                    if (o1.getPriority() > o2.getPriority())
                        return -1;
                    if (o1.getPriority() < o2.getPriority())
                        return 1;
                    return 0;
                }
        ).collect(Collectors.toList());
        collect.forEach(each -> {
            String id = each.getId();
            if (each.getType().equals(1)) {
                id = each.getRelatedManufactureOrderId();
            }

            if (dict.containsKey(id)) {
                dict.get(id).add(each);
            } else {
                List<ManufacturerOrder> list = new ArrayList<>();
                list.add(each);
                dict.put(id, list);
            }
        });

        int listSize = dict.size();
        List<ManufacturerOrder> list1 = new ArrayList<>();
        List<ManufacturerOrder> list2 = new ArrayList<>();
        int i = 0;
        if (listSize == 1) {
            list1 = manufacturerOrderList;
        } else if (listSize > 1) {
            for (String key : dict.keySet()) {
                if (i < (listSize / 2)) {
                    list1.addAll(dict.get(key));
                } else {
                    list2.addAll(dict.get(key));
                }
                i++;
            }
        }

        ArrayList<List<ManufacturerOrder>> res = new ArrayList<>();
        if (list1.size() >= list2.size()) {
            res.add(list1);
            res.add(list2);
        } else {
            res.add(list2);
            res.add(list1);
        }

        return res;
    }


    public static List<Task> generateTaskList(List<ManufacturerOrder> manufacturerOrderList) {
        List<Task> taskList = new ArrayList<>();
//        ManufacturerOrder order = manufacturerOrderList.get(0);
        List<Task> beforeDiepianTask = new ArrayList<>();
        List<Task> afterDiepianTask = new ArrayList<>();
        for (ManufacturerOrder order : manufacturerOrderList) {
            Product product = order.getProduct();
            Integer priority = order.getPriority();
            List<Step> stepList = product.getStepList();
            int stepIndex = 0;
            int orderIndex = order.getIndex();
            int orderIsComplete = order.getIsComplete();
            int delayDays = order.getDelayDays();
            for (Step step : stepList) {
//                //测试代码，一会儿删
//                if(stepIndex<3){
//                    step.setShiftType("1");
//                }else if(stepIndex ==3){
//                    step.setShiftType("2");
//                }
                if(StringUtils.isBlank(step.getIsFinished())){
                    step.setIsFinished("1");
                }
                List<ResourceRequirement> resourceRequirementList = step.getResourceRequirementList();
                List<Task> list = step.getTaskList();
                Integer taskIndex = 0;
                for (Task task : list) {
                    task.setTaskShiftType(step.getShiftType());
                    task.setOrderIsComplete(orderIsComplete);
                    task.setPriority(priority);
                    task.setTaskIndex(taskIndex);
                    task.setProduct(product);
                    task.setStepIndex(stepIndex);
                    if (task.getOrderIndex() == null) {
                        task.setOrderIndex(orderIndex);
                    }
                    task.setProductId(product.getId());
                    task.setStepId(step.getId());
                    task.setOrderId(order.getId());
                    if (task.getOrderType() == null) {
                        task.setOrderType(order.getType());
                    }
                    if (task.getQuantity() == null) {
                        task.setQuantity(task.getTaskQuantity());
//                        task.setQuantity((int) (order.getQuantity()*step.getBrokenRate()));
//                        task.setTaskQuantity(order.getQuantity());
                    }
                    if (order.getType() == 1 && order.getRelatedManufactureOrderId() != null) {
                        task.setRelatedOrderId(order.getRelatedManufactureOrderId());
                    }
                    if(task.getOrderDelayDays()==null){
                        task.setOrderDelayDays(delayDays);

                    }

                    //duration 还得修改
                    task.setDuration((int) Math.ceil((double) order.getQuantity() / task.getSpeed()));
                    task.setSingleTimeSlotSpeed(BigDecimal.valueOf(task.getSpeed()).divide(BigDecimal.valueOf(3), 4, RoundingMode.CEILING));
//                    task.setTimeSlotDuration(BigDecimal.valueOf(order.getQuantity()).divide(task.getSingleTimeSlotSpeed(), 4, RoundingMode.CEILING));
                    task.setMinutesDuration((int) Math.ceil(24.0 * 60 * order.getQuantity() / task.getSpeed()));
//                    if (task.getHalfHourDuration() == null) {
//                        task.setHalfHourDuration((int) Math.ceil(48.0 * order.getJoinQuantity() / task.getSpeed()));
//                    }
//                    task.setHalfHourDuration((int) Math.ceil(48.0 * order.getQuantity() / task.getSpeed()));
                    if (task.getHoursDuration() == null) {
                        if(task.getTaskShiftType().equals("3")){
                            task.setHoursDuration((int) Math.ceil(24.0 * task.getTaskQuantity() / task.getSpeed()));
                        }else if(task.getTaskShiftType().equals("2")){
                            task.setHoursDuration((int) Math.ceil(16.0 * task.getTaskQuantity() / task.getSpeed()));
                        }else{
                            task.setHoursDuration((int) Math.ceil(8.0 * task.getTaskQuantity() / task.getSpeed()));
                        }
                    }
                    task.setManufacturerOrder(order);
                    task.setRequiredResourceId(resourceRequirementList.get(0).getId());
                    taskIndex++;

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

            int diePianIndex = 0;


            for(int i=0;i<stepList.size();i++){
                Step step = stepList.get(i);
                List<Task> stepTasks = step.getTaskList();
                Task stepTask = stepTasks.get(0);
                if(stepTask.getUnit() ==0){
                    beforeDiepianTask.addAll(stepTasks);
                }
                if(stepTask.getUnit() ==1){
                    diePianIndex = i;
                    break;
                }
            }
            if(diePianIndex == 0){
                diePianIndex = stepList.size();
            }

            for(int i=diePianIndex;i<stepList.size();i++){
                Step step = stepList.get(i);
                List<Task> stepTasks = step.getTaskList();
                afterDiepianTask.addAll(stepTasks);
            }
        }

        beforeDiepianTask.forEach(i->i.setIsBeforeDiepian(true));
        afterDiepianTask.forEach(i->i.setIsBeforeDiepian(false));

        Map<String, Map<Integer, List<Task>>> orderIdToLayerNumberToTasks =
                beforeDiepianTask.parallelStream().collect(Collectors.groupingBy(Task::getOrderId, Collectors.groupingBy(Task::getLayerNum)));
        orderIdToLayerNumberToTasks.forEach(
                (orderId, map) -> {
                    System.out.println("orderId"+orderId);
                    map.forEach((layerNumber, tasks) -> {
                        System.out.println("layernnum"+layerNumber);
                        //看是否需要对tasks按照id进行排序
                        List<Task> sortedTasks = tasks.stream()
                                .sorted(Comparator.comparing(Task::getLayerNum))
                                .collect(Collectors.toList());
                        for (int i = 0; i < sortedTasks.size(); i++) {
                            if (i != sortedTasks.size() - 1) {
                                Task current = sortedTasks.get(i);
                                Task next = sortedTasks.get(i + 1);
                                current.setNextTask(next);
                                next.setPreTask(current);
                            }
                        }
                    });
                }
        );        //对每个unit = 0的单个任务设置
//
//        Map<String, Map<Integer, List<Task>>> orderIdToLayerNumberToTasks =
//                taskList.parallelStream().filter(task -> task.getUnit() == 0).collect(Collectors.groupingBy(Task::getOrderId, Collectors.groupingBy(Task::getLayerNum)));
//        orderIdToLayerNumberToTasks.forEach(
//                (orderId, map) -> {
//                    map.forEach((layerNumber, tasks) -> {
//                        //看是否需要对tasks按照id进行排序
//                        for (int i = 0; i < tasks.size(); i++) {
//                            if (i != tasks.size() - 1) {
//                                Task current = tasks.get(i);
//                                Task next = tasks.get(i + 1);
//                                current.setNextTask(next);
//                                next.setPreTask(current);
//                            }
//                        }
//                    });
//                }
//        );
        System.out.println("test");
        //对每个unit=1的套型任务的设置
        Map<String, List<Task>> orderIdToTasks =
                afterDiepianTask.parallelStream().collect(Collectors.groupingBy(Task::getOrderId));
        orderIdToTasks.forEach((orderId, tasks) -> {
            for (int i = 0; i < tasks.size(); i++) {
                if (i != tasks.size() - 1) {
                    Task current = tasks.get(i);
                    Task next = tasks.get(i + 1);
                    current.setNextTask(next);
                    next.setPreTask(current);
                }
            }
        });

//        连接relatedlayer和套型的task
//        for (Task task : taskList) {
//            Integer unit = task.getUnit();
//            if(unit==1){
//                List<Integer> relatedLayer = task.getRelatedLayer();
//                if(relatedLayer!=null){
//                    String currentTaskId = task.getId();
//                    String orderId = task.getOrderId();
//                    Map<Integer,List<Task>> layerNumberToTasks= taskList.parallelStream().
//                            filter(task1 -> task1.getLayerNum()!=null && task1.getOrderId().equals(orderId) && relatedLayer.contains(task1.getLayerNum()))
//                            .collect(Collectors.groupingBy(Task::getLayerNum));
//                    List<List<Task>> taskGroups = layerNumberToTasks.values().stream().collect(Collectors.toList());
//                    for(int i=0;i<taskGroups.size();i++){
//                        if(i!=taskGroups.size()-1){
//                            List<Task> preTasks = taskGroups.get(i);
//                            Task preTask = preTasks.get(preTasks.size()-1);
//                            List<Task> nextTasks = taskGroups.get(i+1);
//                            Task nextTask = nextTasks.get(0);
//                            preTask.setNextTask(nextTask);
//                            nextTask.setPreTask(preTask);
//
//                        }
//                        else{
//                            List<Task> preTasks = taskGroups.get(i);
//                            Task preTask = preTasks.get(preTasks.size()-1);
//                            preTask.setNextTask(task);
//                            task.setPreTask(preTask);
//                        }
//                    }
//
//
//                }
//
//            }
//        }
//        DataGenerator.createAfterIntegratedNormalTaskList(manufacturerOrderList);
//        System.out.println(taskList.size());
//        Collections.reverse(taskList);
        taskList.forEach(task -> {
            if (task.getNextTask() != null) {
                System.out.println("id:" + task.getId() + " next:" + task.getNextTask().getId());
//                System.out.println("ispublic: "+task.getIsPublic()+" orderid: "+task.getOrderId() +" RElated-orderid"+task.getRelatedOrderId()+" "+"taskid: "+task.getId()+" "+
//                        "ordertype "+task.getOrderType()+" "+"related task id "+task.getRelatedTaskId() +" unit : "+task.getUnit()
//                        +" next:"+task.getNextTask().getId()+"step index:"+task.getStepIndex()+" task index"+task.getTaskIndex());
            }else{
                System.out.println("id:" + task.getId() + " next:" + null);

            }

        });
        System.out.println("你好呀"+taskList.size());
//        setSplitQuantity(taskList);
        return taskList;
    }

    public static void main(String[] args) {
//        Integer i = 29;
//        System.out.println(Math.ceil(52.0 / 17));
        Input input = LoadFile.readJsonFile(FILE_PATH);
        List<ManufacturerOrder> manufacturerOrders = DataGenerator.generateOrderList(input);

        List<ResourceItem> resourceItems = DataGenerator.generateResources(input);
        List<Task> tasks = DataGenerator.generateTaskList(manufacturerOrders);
        tasks.stream().map(Task::getHalfHourDuration).forEach(System.out::println);
//        joinList(tasks);
//        joinOrderList(manufacturerOrders);
//        resourceItems.forEach(i->System.out.println(i.toString()));


    }

    //随机挑选一个小样单作为正常单的后续步骤
    public static List<Task> createAfterIntegratedNormalTaskList(List<ManufacturerOrder> orderList) {
        List<Task> tasks = new ArrayList<>();

        Map<String, List<ManufacturerOrder>> orderIdToOrderList =
                orderList.parallelStream().filter(order -> order.getRelatedManufactureOrderId() != null).collect(Collectors.groupingBy(ManufacturerOrder::getRelatedManufactureOrderId));
        orderIdToOrderList.forEach((k, v) -> {

            //v是小样单
            Integer sum = v.stream().mapToInt(ManufacturerOrder::getQuantity).sum();
            orderList.stream().filter(manufacturerOrder -> manufacturerOrder.getId().equals(k)).
                    forEach(i -> {
                        i.setJoinQuantity(i.getQuantity() + sum);
                        Integer demoStepListSize = v.get(0).getProduct().getStepList().size();
                        Integer normalStepListSize = i.getProduct().getStepList().size();

                        if (demoStepListSize > normalStepListSize) {
                            List<Step> demoDifferentStepList = v.get(0).getProduct().getStepList().subList(normalStepListSize, demoStepListSize);
                            for (int index = 0; index < demoDifferentStepList.size(); index++) {
                                Step step = demoDifferentStepList.get(index);
                                for (int taskIndex = 0; taskIndex < step.getTaskList().size(); taskIndex++) {
                                    Task task = step.getTaskList().get(taskIndex);
                                    if (index == 0 && taskIndex == 0) {
                                        task.setIsSplit(true);
                                    }
                                    task.setIsPublic(false);
                                    task.setRelatedTaskId(task.getId());
                                    task.setId(task.getId() + "-demo");
                                    task.setOrderType(i.getType());
                                    task.setOrderIndex(i.getIndex());
                                    Integer quantity = i.getQuantity();
                                    Integer delayDays = i.getDelayDays();
                                    task.setQuantity(quantity);
                                    task.setHoursDuration((int) Math.ceil(24.0 * quantity / task.getSpeed()));
                                    task.setOrderDelayDays(delayDays);
                                    tasks.add(task);

                                }
                            }
                            demoDifferentStepList.forEach(j -> j.getTaskList().forEach(
                                    task -> {
                                        task.setRelatedTaskId(task.getId());
                                        task.setId(task.getId() + "-demo");
                                        task.setOrderType(i.getType());
                                        task.setOrderIndex(i.getIndex());
                                        Integer quantity = i.getQuantity();
                                        Integer delayDays = i.getDelayDays();
                                        task.setQuantity(quantity);
                                        task.setHoursDuration((int) Math.ceil(24.0 * quantity / task.getSpeed()));
                                        task.setOrderDelayDays(delayDays);
                                        tasks.add(task);
                                    }
                            ));
                            i.getProduct().getStepList().addAll(demoDifferentStepList);

                        }

                    });
        });
//        Map<String, Map<String, List<Task>>> orderIdToRelatedOrderIdToTasks =
//                taskList.parallelStream().filter(
//                        i -> i.getUnit() != null && i.getUnit() == 1 && i.getOrderType() == 1).collect(Collectors.groupingBy(Task::getRelatedOrderId, Collectors.groupingBy(Task::getOrderId)));
//        List<Task> tasks = new ArrayList<>();
//        orderIdToRelatedOrderIdToTasks.forEach((k,v)->{
//            Set<String> strings = v.keySet();
//            Iterator<String> iterator = strings.iterator();
//            if(iterator.hasNext()){
//                String next = iterator.next();
//                List<Task> singleTaskList = v.get(next);
//                tasks.addAll(singleTaskList);
//            }
//        });
//        for (Task task:tasks){
//            String relatedOrderId = task.getRelatedOrderId();
//            List<ManufacturerOrder> manufacturerOrders = orderList.stream().filter(i -> i.getId().equals(relatedOrderId)).collect(Collectors.toList());
//            Integer quantity = manufacturerOrders.get(0).getQuantity();
//            Integer delayDays = manufacturerOrders.get(0).getDelayDays();
//            task.setQuantity(quantity);
//            task.setHoursDuration((int) Math.ceil(24.0 * quantity / task.getSpeed()));
//            task.setOrderDelayDays(delayDays);
//        }
        return tasks;
    }

    private void reverse(Task task) {

    }

    public static void joinList(List<Task> taskList) {

        Map<String, List<Task>> relatedOrderIdToTasks =
                taskList.parallelStream().filter(task -> task.getRelatedOrderId() != null).collect(Collectors.groupingBy(Task::getRelatedOrderId));
        relatedOrderIdToTasks.forEach((s, taskList1) -> {
            System.out.println(s + "数量" + taskList1.size());
        });
        //        List<Task> collect = taskList.stream().flatMap(task1 ->
//                taskList.stream().filter(
//                        task2 ->
//                                task1.getRelatedOrderId()!=null&&task1.getRelatedOrderId().equals(task2.getOrderId()) && task1.getStepIndex() == task2.getStepIndex() &&
//                                        task1.getLayerNum() == task2.getLayerNum()
//                ).map(task ->{
//                    task.setSpeed(task.getSpeed()+task1.getSpeed());
//                    System.out.println(task.getOrderId()+" "+task1.getOrderId());
//                    return  task;
//                })).collect(Collectors.toList());
//        collect.forEach(i->{
//            System.out.println(i.getOrderId()+" "+i.getId() +"speed:"+i.getSpeed());
//        });


    }

    public static void setSplitQuantity(List<Task> taskList) {

        Map<String, List<Task>> relatedOrderIdToTasks = taskList.parallelStream().filter(task -> task.getIsPublic() && task.getRelatedOrderId() != null).collect(Collectors.groupingBy(Task::getRelatedOrderId));
        relatedOrderIdToTasks.forEach((s, taskList1) -> {
            for (Task task : taskList) {
                List<String> demoTaskIdList = new ArrayList<>();
                List<Integer> demoTaskQuantityList = new ArrayList<>();
                List<Integer> demoTaskDurationList = new ArrayList<>();
                List<Integer> demoTaskMinutesDurationList = new ArrayList<>();

                if (task.getOrderId().equals(s)) {
                    for (Task demoTask : taskList1) {
                        if (Objects.equals(demoTask.getStepIndex(), task.getStepIndex()) && Objects.equals(demoTask.getTaskIndex(), task.getTaskIndex())) {
                            demoTaskIdList.add(demoTask.getId());
                            demoTaskQuantityList.add(demoTask.getTaskQuantity());
                            demoTaskDurationList.add(demoTask.getHoursDuration());
                            demoTaskMinutesDurationList.add(demoTask.getMinutesDuration());
                        }
                    }
                    String demoTaskId = Joiner.on(",").join(demoTaskIdList);
                    String demoTaskQuantity = Joiner.on(",").join(demoTaskQuantityList);
                    Integer demoTaskDurationSum = demoTaskDurationList.stream().reduce(Integer::sum).orElse(0);
                    Integer demoTaskMinutesDurationSum = demoTaskMinutesDurationList.stream().reduce(Integer::sum).orElse(0);
                    String demoTaskDuration = Joiner.on(",").join(demoTaskDurationList);
                    String demoTaskMinutesDuration = Joiner.on(",").join(demoTaskMinutesDurationList);
                    int duration = task.getHoursDuration();
                    int minutesDuration = task.getMinutesDuration();
                    task.setDemoTaskId(demoTaskId);
                    int sumMinutes = minutesDuration+demoTaskDurationSum;
                    if(Math.ceil(1.0*sumMinutes) <= 60){
                        task.setHoursDuration(duration);
                    }else{
                        int d = (int) (Math.ceil(1.0*sumMinutes)  / 60 + 1);
                        task.setHoursDuration(d);
                    }
//                    task.setHoursDuration(duration + demoTaskDurationSum);
                    task.setDemoTaskQuantity(demoTaskQuantity);
                    task.setDemoTaskDuration(demoTaskDuration);
                    task.setDemoTaskMinutesDuration(demoTaskMinutesDuration);
                }
            }
        });

    }

    public static void joinOrderList(List<ManufacturerOrder> orderList) {
        for (int i = 0; i < orderList.size(); i++) {
            ManufacturerOrder order = orderList.get(i);
            order.setIndex(i);
            order.setJoinQuantity(order.getQuantity());
        }
//        orderList.forEach(i->i.setJoinQuantity(i.getQuantity()));
        Map<String, List<ManufacturerOrder>> orderIdToOrderList =
                orderList.parallelStream().filter(order -> order.getRelatedManufactureOrderId() != null).collect(Collectors.groupingBy(ManufacturerOrder::getRelatedManufactureOrderId));

        orderIdToOrderList.forEach((k, v) -> {
            //v是小样单
            Integer sum = v.stream().mapToInt(ManufacturerOrder::getQuantity).sum();
            for (ManufacturerOrder order : orderList) {
                if (order.getId().equals(k)) {
                    if (order.getIsComplete() == 0) {

                        order.setJoinQuantity(order.getQuantity() + sum);
                        Integer demoStepListSize = v.get(0).getProduct().getStepList().size();
                        Integer normalStepListSize = order.getProduct().getStepList().size();

                        if (demoStepListSize > normalStepListSize) {
                            //将挑选的随机单设为ispublic false
                            List<Step> demoDifferentStepList = v.get(0).getProduct().getStepList().subList(normalStepListSize, demoStepListSize);
                            demoDifferentStepList.stream().forEach(i -> i.getTaskList().forEach(j -> j.setIsPublic(false)));
                           //也得把其他的设为ispublic false
                            v.forEach(otherV->{
                                List<Step> otherDemoDifferentStepList = otherV.getProduct().getStepList().subList(normalStepListSize, demoStepListSize);
                                otherDemoDifferentStepList.stream().forEach(i -> i.getTaskList().forEach(j -> j.setIsPublic(false)));

                            });
                            List<Step> copy = new ArrayList<>();
                            for (int idx = 0; idx < demoDifferentStepList.size(); idx++) {
                                Step step = new Step();
                                step.setId(demoDifferentStepList.get(idx).getId());
                                step.setCode(demoDifferentStepList.get(idx).getCode());
                                step.setName(demoDifferentStepList.get(idx).getName());
                                step.setStepStartTime(demoDifferentStepList.get(idx).getStepStartTime());
                                step.setExecutionDays(demoDifferentStepList.get(idx).getExecutionDays());
                                step.setResourceRequirementList(demoDifferentStepList.get(idx).getResourceRequirementList());
                                List<Task> list = demoDifferentStepList.get(idx).getTaskList();
                                List<Task> newList = new ArrayList<>();
                                for (int a = 0; a < list.size(); a++) {
                                    Task item = list.get(a);
                                    Task task = new Task();
                                    BeanUtils.copyProperties(item, task);
//                                task.setId("");
//                                task.setCode("");
//                                task.setSpeed(0);
//                                task.setUnit(0);
//                                task.setTaskOrder("");
//                                task.setLayerNum(0);
//                                task.setRelatedLayer("");
//                                task.setProduct(new Product());
//                                task.setTaskType(TaskType.SOURCE);
//                                task.setStartTime(0);
//                                task.setEndTime(0);
//                                task.setReadyTime(0);
//                                task.setRequiredResourceId("");
//                                task.setRequiredResourceIntId(0);
//                                task.setSchedule(0);
//                                task.setTime(LocalDateTime.now());
//                                task.setAmount(0);
//                                task.setProductId("");
//                                task.setStepId("");
//                                task.setStepIndex(0);
//                                task.setManufacturerOrder(new ManufacturerOrder());
//                                task.setPreTask(new Task());
//                                task.setDuration(0);
//                                task.setTimeSlotDuration(new BigDecimal("0"));
//                                task.setSingleTimeSlotSpeed(new BigDecimal("0"));
//                                task.setMinutesDuration(0);
//                                task.setOrderId("");
//                                task.setHalfHourDuration(0);
//                                task.setHoursDuration(0);
//                                task.setOrderIndex(0);
//                                task.setQuantity(0);
//                                task.setRelatedOrderId("");
//                                task.setOrderType(0);
//                                task.setRelatedQuantity(0);
//                                task.setOrderDelayDays(0);
//                                task.setRelatedTaskId("");
//                                task.setIsSplit(false);
//                                task.setIsPublic(false);
//                                task.setTaskBeginTime(LocalDateTime.now());

                                    newList.add(task);

                                }
                                step.setTaskList(newList);
                                step.setAssignedTaskList(new ArrayList<>());
                                step.setProductId(demoDifferentStepList.get(idx).getProductId());
                                copy.add(step);
                            }
                            for (int index = 0; index < copy.size(); index++) {
                                Step step = copy.get(index);
                                for (int taskIndex = 0; taskIndex < step.getTaskList().size(); taskIndex++) {
                                    Task task = step.getTaskList().get(taskIndex);
                                    if (index == 0 && taskIndex == 0) {
                                        task.setIsSplit(true);
                                    }
                                    task.setIsPublic(false);
                                    task.setOrderId(order.getId());
                                    task.setRelatedTaskId(task.getId());
                                    task.setId(task.getId() + "-demo");
                                    task.setOrderType(order.getType());
                                    task.setOrderIndex(order.getIndex());
                                    Integer quantity = order.getQuantity();
                                    Integer delayDays = order.getDelayDays();
                                    task.setQuantity(quantity);
                                    task.setTaskQuantity(quantity);
                                    task.setHoursDuration((int) Math.ceil(24.0 * 3* quantity / task.getSpeed()));
                                    task.setOrderDelayDays(delayDays);

                                }
                            }
//                            demoDifferentStepList.forEach(j->j.getTaskList().forEach(
//                                    task -> {
//                                        task.setRelatedTaskId(task.getId());
//                                        task.setId(task.getId()+"-demo");
//                                        task.setOrderType(i.getType());
//                                        task.setOrderIndex(i.getIndex());
//                                        Integer quantity = i.getQuantity();
//                                        Integer delayDays = i.getDelayDays();
//                                        task.setQuantity(quantity);
//                                        task.setHoursDuration((int) Math.ceil(24.0 * quantity / task.getSpeed()));
//                                        task.setOrderDelayDays(delayDays);
//                                    }
//                            ));
                            order.getProduct().getStepList().addAll(copy);
                            order.getProduct().getStepList().forEach(d -> d.getTaskList().forEach(System.out::println));

                        }
                    }else if(order.getIsComplete() == 1){
                        // do nothing
                    }
                }
            }
//            orderList.stream().filter(manufacturerOrder -> manufacturerOrder.getId().equals(k)).
//                    forEach(i -> {
//                        i.setJoinQuantity(i.getQuantity() + sum);
//                        Integer demoStepListSize =  v.get(0).getProduct().getStepList().size();
//                        Integer normalStepListSize  =  i.getProduct().getStepList().size();
//
//                        if(demoStepListSize>normalStepListSize){
//                            List<Step> demoDifferentStepList =  v.get(0).getProduct().getStepList().subList(normalStepListSize,demoStepListSize);
//                            List<Step> copy = new ArrayList<>();
//                            for(int idx =0;idx <demoDifferentStepList.size();idx++){
//                                Step step = new Step();
//                                BeanUtils.copyProperties(demoDifferentStepList.get(idx),step);
//                                copy.add(step);
//                            }
//                            for(int index= 0;index<copy.size();index++){
//                                Step step = copy.get(index);
//                                for(int taskIndex = 0;taskIndex<step.getTaskList().size();taskIndex++){
//                                    Task task = step.getTaskList().get(taskIndex);
//                                    if(index==0&&taskIndex==0){
//                                        task.setIsSplit(true);
//                                    }
//                                    task.setIsPublic(false);
//                                    task.setRelatedTaskId(task.getId());
//                                    task.setId(task.getId()+"-cmd");
//                                    task.setOrderType(i.getType());
//                                    task.setOrderIndex(i.getIndex());
//                                    Integer quantity = i.getQuantity();
//                                    Integer delayDays = i.getDelayDays();
//                                    task.setQuantity(quantity);
//                                    task.setHoursDuration((int) Math.ceil(24.0 * quantity / task.getSpeed()));
//                                    task.setOrderDelayDays(delayDays);
//
//                                }
//                            }
////                            demoDifferentStepList.forEach(j->j.getTaskList().forEach(
////                                    task -> {
////                                        task.setRelatedTaskId(task.getId());
////                                        task.setId(task.getId()+"-demo");
////                                        task.setOrderType(i.getType());
////                                        task.setOrderIndex(i.getIndex());
////                                        Integer quantity = i.getQuantity();
////                                        Integer delayDays = i.getDelayDays();
////                                        task.setQuantity(quantity);
////                                        task.setHoursDuration((int) Math.ceil(24.0 * quantity / task.getSpeed()));
////                                        task.setOrderDelayDays(delayDays);
////                                    }
////                            ));
//                            i.getProduct().getStepList().addAll(copy);
//                            i.getProduct().getStepList().forEach(d->d.getTaskList().forEach(System.out::println));
//
//                        }
//                    });
        });
        orderList.forEach(i -> System.out.println(i.getIndex() + " id" + i.getId()));
//        List<ManufacturerOrder> collect = orderList.stream().flatMap(order1 ->
//                orderList.stream().
//                        filter(order2->
//                        order2.getRelatedManufactureOrderId()!=null&&
//                                order2.getRelatedManufactureOrderId().equals(order1.getId())).
//                        map(order2 ->{order2.setQuantity(order1.getQuantity()+ order2.getQuantity());
//                            return  order2;
//                        })).collect(Collectors.toList());
//        collect.forEach(i->{
//            System.out.println(i.getId()+" "+i.getQuantity());
//        });

    }

}
