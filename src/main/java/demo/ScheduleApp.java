package demo;//package demo;
//
//
//import demo.domain.Schedule;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.time.Duration;
//import java.util.Comparator;
//import java.util.List;
//
//public class ScheduleApp {
//    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleApp.class);
//    public static void main(String[] args) {
//        SolverFactory<Schedule> solverFactory = SolverFactory.create(new SolverConfig()
//                .withSolutionClass(Schedule.class)
//                .withEntityClasses(Allocation.class)
//                .withConstraintProviderClass(TimeTableConstraintProvider.class)
//                // The solver runs only for 5 seconds on this small dataset.
//                // It's recommended to run for at least 5 minutes ("5m") otherwise.
//                .withTerminationSpentLimit(Duration.ofSeconds(5)));
//
//        // Load the problem
//        Schedule problem = generateDemoData();
//
//        // Solve the problem
//        Solver<Schedule> solver = solverFactory.buildSolver();
//        Schedule solution = solver.solve(problem);
//        ScoreManager<Schedule, HardSoftScore> scoreManager = ScoreManager.create(solverFactory);
//        System.out.println(scoreManager.explainScore(solution));
//        ScoreExplanation<Schedule, HardSoftScore> scoreExplanation = scoreManager.explainScore(problem);
//        HardSoftScore score = scoreExplanation.getScore();
//        System.out.println(score);
//        // Visualize the solution
//        printTimetable(solution);
//    }
//
//    public static Schedule generateDemoData() {
//        Schedule schedule = new Schedule();
//        List<ScheduleDate> scheduleDates = DataGenerator.generateScheduleDateList();
//        List<ResourceItem> resourceItemList = DataGenerator.generateResources();
//        List<ManufacturerOrder> manufacturerOrders = DataGenerator.generateOrderList();
//        List<Task> taskList = DataGenerator.generateTaskList();
//        DataGenerator.sortTask(taskList);
//        List<Allocation> allocationList = DataGenerator.createAllocationList(taskList, manufacturerOrders);
//        schedule.setTaskList(taskList);
//        schedule.setAllocationList(allocationList);
//        schedule.setResourceList(resourceItemList);
////        schedule.setResourceRequirementList(null);
//        schedule.setManufacturerOrderList(manufacturerOrders);
//        return schedule;
//    }
//        private static void printTimetable(Schedule schedule) {
//            LOGGER.info("");
////            List<ResourceItem> resourceItemList = schedule.getResourceItemList();
//            List<Allocation> allocationList = schedule.getAllocationList();
//            allocationList.sort(Comparator.comparing(Allocation::getActualStartTime));
//            allocationList.forEach(i->{
//                System.out.println("id: "+i.getId()+" "+
//                        "task: "+i.getTask().getId()+" "+
//                        "resource: "+i.getTask().getRequiredResourceId()+" "+
//                        "start_date: "+i.getActualStartTime()+" "+
//                        "delay: "+i.getDelay()+" "+
//                        "end_date :"+i.getActualEndTime()+" "+
//                        "speed: "+i.getTask().getSpeed()
//                );
//            });
//
//        }
//
//}
