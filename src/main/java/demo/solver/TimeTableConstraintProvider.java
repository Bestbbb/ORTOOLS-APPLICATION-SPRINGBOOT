package demo.solver;//package demo.solver;
//
//import com.example.optaplanner_industry.demo.domain.*;
//import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
//import org.optaplanner.core.api.score.stream.*;
//
//import static org.optaplanner.core.api.score.stream.ConstraintCollectors.count;
//import static org.optaplanner.core.api.score.stream.ConstraintCollectors.toList;
//
//public class TimeTableConstraintProvider implements ConstraintProvider {
//
//    @Override
//    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
//        return new Constraint[]{
//                // Hard constraints
////                nonRenewableResourceCapacity(constraintFactory),
////                sameStepResourceConflict(constraintFactory),
////                nonRenewableResourceCapacity(constraintFactory),
//        nonRenewableResourceCapacity2(constraintFactory),
////                nonRenewableResourceCapacity1(constraintFactory)
//
//
////                workGroupConflict(constraintFactory),
////                sameLayerTaskOrderConflict(constraintFactory),
////                sameStepResourceConflict(constraintFactory),
////                sameStepResourceConflict5(constraintFactory),
////                sameStepResourceConflict4(constraintFactory),
////                sameStepResourceConflict3(constraintFactory),
////                sameStepResourceConflict1(constraintFactory),
////                sameStepResourceConflict2(constraintFactory)
////                sameStepResourceConflict1(constraintFactory)
//
////                workConflict(constraintFactory),
////                studentGroupConflict(constraintFactory),
////                workerGroupMatch(constraintFactory)
////                timeConflict(constraintFactory),
////                delayDaysConflict(constraintFactory),
////                differentLayerConflict(constraintFactory),
////                sameLayerConflict(constraintFactory),
////                // Soft constraints
////                exchangeTimeConflict(constraintFactory),
////                priorityConflict(constraintFactory)
////                teacherRoomStability(constraintFactory),
////                teacherTimeEfficiency(constraintFactory),
////                studentGroupSubjectVariety(constraintFactory)
//        };
//    }
//
//    // 同一时刻，同一工序只能在对应工作组生产
//    Constraint workGroupConflict(ConstraintFactory constraintFactory) {
//        // A machine can work at most one task at the same time.
//        return constraintFactory
//                .forEachUniquePair(Task.class,
//                        Joiners.equal(Task::getRequiredResourceId),
//                        Joiners.equal(t -> t.getResourceItem().getResourcePoolId()))
//                .penalize("WorkGroup conflict", HardSoftScore.ONE_HARD);
//    }
//
////    Constraint workConflict(ConstraintFactory constraintFactory) {
////        // A worker can operate at most one machine at the same time.
////        return constraintFactory
////                .forEachUniquePair(Task.class,
////                        Joiners.equal(Task::getTimeslot),
////                        Joiners.equal(Task::getWorker))
////                .penalize("Worker conflict", HardSoftScore.ONE_HARD);
////    }
//
////    Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
////        // A student can attend at most one lesson at the same time.
////        return constraintFactory
////                .forEachUniquePair(Task.class,
////                        Joiners.equal(Task::getScheduleDate),
////                        Joiners.equal(Task::getCode))
////                .penalize("Student group conflict", HardSoftScore.ONE_HARD);
////    }
//
//    // 某一工序只能在对应工作组生产
////    Constraint workerGroupMatch(ConstraintFactory constraintFactory) {
////        // A worker must match to a specific machine.
////        return constraintFactory
////                .forEachUniquePair(Task.class,
//////                        Joiners.equal(Task::getTimeslot),
////                        Joiners.equal((task) -> !Objects.equals(task.getCode(), task.get().getName())))
//////                .filter((task1, task2) -> Objects.equals(task1.getSubject(), task2.getWorkGroup().getName()))
////                .reward("Teacher room stability", HardSoftScore.ofHard(5));
////    }
//
////    Constraint teacherRoomStability(ConstraintFactory constraintFactory) {
////        // A teacher prefers to teach in a single room.
////        return constraintFactory
////                .forEachUniquePair(Task.class,
////                        Joiners.equal(Task::getWorker))
////                .filter((task1, task2) -> task1.getWorkGroup() != task2.getWorkGroup())
////                .penalize("Teacher room stability", HardSoftScore.ONE_SOFT);
////    }
////
////    Constraint teacherTimeEfficiency(ConstraintFactory constraintFactory) {
////        // A teacher prefers to teach sequential lessons and dislikes gaps between lessons.
////        return constraintFactory
////                .forEach(Task.class)
////                .join(Task.class, Joiners.equal(Task::getWorker),
////                        Joiners.equal((task) -> task.getTimeslot().getDayOfWeek()))
////                .filter((task1, task2) -> {
////                    Duration between = Duration.between(task1.getTimeslot().getEndTime(),
////                            task2.getTimeslot().getStartTime());
////                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
////                })
////                .reward("Teacher time efficiency", HardSoftScore.ONE_SOFT);
////    }
//
////    Constraint studentGroupSubjectVariety(ConstraintFactory constraintFactory) {
////        // A student group dislikes sequential lessons on the same subject.
////        return constraintFactory
////                .forEach(Task.class)
////                .join(Task.class,
////                        Joiners.equal(Task::getCode),
////                        Joiners.equal(Task::getTaskOrder),
////                        Joiners.equal((task) -> task.getTimeslot().getDayOfWeek()))
////                .filter((task1, task2) -> {
////                    Duration between = Duration.between(task1.getTimeslot().getEndTime(),
////                            task2.getTimeslot().getStartTime());
////                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
////                })
////                .penalize("Student group subject variety", HardSoftScore.ONE_SOFT);
////    }
//
////    //Hard constraint
//    Constraint sameLayerTaskOrderConflict(ConstraintFactory constraintFactory) {
//        return constraintFactory
//                .forEachUniquePair(Task.class,Joiners.equal(Task::getLayerNum),Joiners.filtering((task1,task2)->
//                        task2.getStepIndex()-task1.getStepIndex()==1))
//                .filter((task1,task2)->{
//                    System.out.println("task1："+task1.getScheduleDate().getLocalDateTime()+"task2:"+task2.getScheduleDate().getLocalDateTime());
//                           return task1.getScheduleDate().getLocalDateTime().isAfter(task2.getScheduleDate().getLocalDateTime());
//
//                        }
//                        )
//                .penalize("1",HardSoftScore.ofHard(200));
//
//    }
//    Constraint sameLayerTaskOrderConflict1(ConstraintFactory constraintFactory) {
//        return constraintFactory
//                .forEach(Task.class)
//                .groupBy(Task::getLayerNum,toList())
//                .filter((layerNumber,list)->{
//                    return false;
//                })
//                .penalize("1",HardSoftScore.ofHard(200));
//
//    }
//
//    Constraint sameStepResourceConflict(ConstraintFactory constraintFactory) {
//
//        return constraintFactory
//                .forEach(Task.class)
//                .filter(task -> task.getResourceItem().getResourcePoolId().equals(task.getRequiredResourceId()))
//                .reward("123123213312313123123", HardSoftScore.ofHard(100));
//    }
//    Constraint sameStepResourceConflict2(ConstraintFactory constraintFactory) {
//
//        return constraintFactory
//                .forEach(Task.class)
//                .filter(task -> task.getActualEndTime().equals(task.getTaskBeginTime()))
//
//              .penalize("dasd312123",HardSoftScore.ofHard(100));
//    }
//    Constraint sameStepResourceConflict4(ConstraintFactory constraintFactory) {
//
//        return constraintFactory
//                .forEach(Task.class)
//                .groupBy(Task::getActualStartTime,count())
//
//                .penalize("dasd",HardSoftScore.ofHard(100),(time,count)->
//                {if(count>1){ return count;}
//                return 0;});
//    }
//    Constraint sameStepResourceConflict5(ConstraintFactory constraintFactory) {
//
//        return constraintFactory
//                .forEach(Task.class)
//                .map(Task::getActualStartTime)
//                .distinct()
//                .penalize("41234214234",HardSoftScore.ofHard(500));
//    }
//    Constraint sameStepResourceConflict6(ConstraintFactory constraintFactory) {
//
//        return constraintFactory
//                .forEach(Task.class)
//                .ifExists(Task.class,Joiners.equal(Task::getStepId))
//                .ifExists(Task.class,Joiners.equal(Task::getActualStartTime))
//                .penalize("41234241241414234",HardSoftScore.ofHard(100));
//    }
//
//    Constraint sameStepResourceConflict3(ConstraintFactory constraintFactory) {
//
//        return constraintFactory
//                .forEachUniquePair(Task.class,Joiners.equal(Task::getStepId))
//                .filter((task, task2) -> !task.getActualStartTime().equals(task2.getActualStartTime()))
//                .reward("dasd",HardSoftScore.ofHard(100));
//    }
//
//    Constraint sameStepResourceConflict1(ConstraintFactory constraintFactory) {
//
//        return constraintFactory
//                .forEach(Task.class)
//                .filter(task -> task.getResourceItem()!=null)
//                .join(ResourceItem.class,Joiners.filtering((task,item)->task.getRequiredResourceId().equals(item.getResourcePoolId())))
//                .filter((task,item) -> task.getResourceItem().getResourcePoolId().equals(task.getRequiredResourceId()))
//                .reward("sameStepResourceConflict1", HardSoftScore.ofHard(100));
//    }
//
//    Constraint timeConflict(ConstraintFactory constraintFactory) {
//        return constraintFactory
//                .forEach(ManufacturerOrder.class)
//                .filter(order -> order.getPeriod().getEndTime().isBefore(order.getEndDate()))
//                .penalize("time late", HardSoftScore.ofHard(1));
//    }
//
//    Constraint delayDaysConflict(ConstraintFactory constraintFactory) {
//        return constraintFactory
//                .forEach(ManufacturerOrder.class)
//                .filter(manufacturerOrder -> manufacturerOrder.getType() == 0)
//                .filter(order -> order.getEndDate() != null &&
//                        order.getTotalDays() > order.getPeriod().getRequiredDuration())
//                .penalize("time late", HardSoftScore.ofHard(1),
//                        order -> order.getTotalDays() - order.getPeriod().getRequiredDuration());
//    }
//
//    Constraint differentLayerConflict(ConstraintFactory constraintFactory) {
//        return constraintFactory
//                .forEach(Task.class)
//                .filter(task -> task.getUnit() == 1)
//                .join(Task.class, Joiners.filtering((task1, task2) -> task1.getRelatedLayer().contains(task2.getLayerNum())))
//                .join(Task.class, Joiners.filtering((task1, task2, task3) -> task2.getLayerNum() < task3.getLayerNum()))
//                .penalize("Different layer conflict", HardSoftScore.ofHard(1));
//
//    }
//
//    Constraint sameLayerConflict(ConstraintFactory constraintFactory) {
//        return constraintFactory
//                .forEachUniquePair(Layer.class, Joiners.equal(Layer::getProduct))
//                .filter(((layer, layer2) -> layer.getLayerNumber() > layer2.getLayerNumber()))
//                .penalize("One layer conflict", HardSoftScore.ofHard(5));
//    }
//
//    //Soft Constraint
//    Constraint exchangeTimeConflict(ConstraintFactory constraintFactory) {
//        return constraintFactory
//                .forEach(ResourceItem.class)
//                .groupBy(ResourceItem::getCode, count())
//                .penalize("min product change", HardSoftScore.ofSoft(1), (product, integer) -> integer);
//
//    }
//
//    Constraint priorityConflict(ConstraintFactory constraintFactory) {
//        return constraintFactory
//                .forEachUniquePair(ManufacturerOrder.class,
//                        Joiners.lessThanOrEqual(ManufacturerOrder::getPriority))
//                .reward("reward priority", HardSoftScore.ofSoft(1));
//
//    }
//
//    protected Constraint nonRenewableResourceCapacity(ConstraintFactory constraintFactory) {
//        return constraintFactory.forEach(Allocation.class)
//                .join(Task.class)
//                .filter((allocation,task)->{
//                   return  allocation.getId().equals(task.getId());
//                })
//                .reward("11",HardSoftScore.ONE_HARD);
//    }
//    protected Constraint nonRenewableResourceCapacity2(ConstraintFactory constraintFactory) {
//        return constraintFactory
//                .forEachUniquePair(Allocation.class,Joiners.equal(Allocation::getStepIndex)
//                ,Joiners.equal(Allocation::getActualStartTime))
//                .penalize("22",HardSoftScore.ONE_HARD);
//
//    }
//    protected Constraint nonRenewableResourceCapacity3(ConstraintFactory constraintFactory) {
//        return constraintFactory
//                .forEachUniquePair(Allocation.class,Joiners.equal(Allocation::getStepIndex))
//                .filter(((allocation, allocation2) -> allocation.getActualStartTime().equals(allocation2.getActualStartTime())))
//                .penalize("33",HardSoftScore.ONE_HARD);
//
//    }
//    protected Constraint nonRenewableResourceCapacity1(ConstraintFactory constraintFactory) {
//        return constraintFactory
//                .forEach(Allocation.class)
//                .groupBy(Allocation::getStartDate, count())
//
//                .penalize("31414423", HardSoftScore.ofHard(1), (time, count) ->
//                {
//                    if (count > 1) {
//                        return count;
//                    }
//                    return 0;
//                });
//    }
//
//
//
////
////    protected Constraint renewableResourceCapacity(ConstraintFactory constraintFactory) {
////        return constraintFactory.forEach(ResourceRequirement.class)
////                .filter(ResourceRequirement::isResourceRenewable)
////                .join(Allocation.class,
////                        Joiners.equal(ResourceRequirement::getExecutionMode, Allocation::getExecutionMode))
////                .flattenLast(a -> IntStream.range(a.getStartDate(), a.getEndDate())
////                        .boxed()
////                        .collect(Collectors.toList()))
////                .groupBy((resourceReq, date) -> resourceReq.getResource(),
////                        (resourceReq, date) -> date,
////                        ConstraintCollectors.sum((resourceReq, date) -> resourceReq.getRequirement()))
////                .filter((resourceReq, date, totalRequirement) -> totalRequirement > resourceReq.getCapacity())
////                .penalize("Renewable resource capacity",
////                        HardMediumSoftScore.ofHard(1),
////                        (resourceReq, date, totalRequirement) -> totalRequirement - resourceReq.getCapacity());
////    }
////
////    protected Constraint totalProjectDelay(ConstraintFactory constraintFactory) {
////        return constraintFactory.forEach(Allocation.class)
////                .filter(allocation -> allocation.getEndDate() != null)
////                .filter(allocation -> allocation.getJobType() == JobType.SINK)
////                .impact("Total project delay",
////                        HardMediumSoftScore.ofMedium(1),
////                        allocation -> allocation.getProjectCriticalPathEndDate() - allocation.getEndDate());
////    }
////
////    protected Constraint totalMakespan(ConstraintFactory constraintFactory) {
////        return constraintFactory.forEach(Allocation.class)
////                .filter(allocation -> allocation.getEndDate() != null)
////                .filter(allocation -> allocation.getJobType() == JobType.SINK)
////                .groupBy(ConstraintCollectors.max(Allocation::getEndDate))
////                .penalize("Total makespan",
////                        HardMediumSoftScore.ofSoft(1),
////                        maxEndDate -> maxEndDate);
////    }
//
//}
