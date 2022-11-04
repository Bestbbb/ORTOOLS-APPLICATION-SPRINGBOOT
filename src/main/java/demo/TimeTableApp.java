package demo;//package com.example.optaplanner_industry.demo;
//
//
//import com.example.optaplanner_industry.demo.bootstrap.DataGenerator;
//import com.example.optaplanner_industry.demo.domain.*;
//import com.example.optaplanner_industry.demo.solver.TimeTableConstraintProvider;
//import org.optaplanner.core.api.score.ScoreExplanation;
//import org.optaplanner.core.api.score.ScoreManager;
//import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
//import org.optaplanner.core.api.solver.Solver;
//import org.optaplanner.core.api.solver.SolverFactory;
//import org.optaplanner.core.config.solver.SolverConfig;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.time.Duration;
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class TimeTableApp {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(TimeTableApp.class);
//
//    public static void main(String[] args) {
//        SolverFactory<TimeTable> solverFactory = SolverFactory.create(new SolverConfig()
//                .withSolutionClass(TimeTable.class)
//                .withEntityClasses(Task.class)
//                .withConstraintProviderClass(TimeTableConstraintProvider.class)
//                // The solver runs only for 5 seconds on this small dataset.
//                // It's recommended to run for at least 5 minutes ("5m") otherwise.
//                .withTerminationSpentLimit(Duration.ofSeconds(5)));
//
//        // Load the problem
//        TimeTable problem = generateDemoData();
//
//        // Solve the problem
//        Solver<TimeTable> solver = solverFactory.buildSolver();
//        TimeTable solution = solver.solve(problem);
//        ScoreManager<TimeTable, HardSoftScore> scoreManager = ScoreManager.create(solverFactory);
//        System.out.println(scoreManager.explainScore(solution));
//        ScoreExplanation<TimeTable, HardSoftScore> scoreExplanation = scoreManager.explainScore(problem);
//        HardSoftScore score = scoreExplanation.getScore();
//        System.out.println(score);
//        // Visualize the solution
//        printTimetable(solution);
//    }
//
//    public static TimeTable generateDemoData() {
//        List<ScheduleDate> scheduleDates = DataGenerator.generateScheduleDateList();
//        List<ResourceItem> resourceItemList = DataGenerator.generateResources();
//        List<Task> taskList = DataGenerator.generateTaskList();
//
//        return new TimeTable(scheduleDates, resourceItemList, taskList);
//    }
//
//    private static void printTimetable(TimeTable timeTable) {
//        LOGGER.info("");
//        List<ResourceItem> resourceItemList = timeTable.getResourceItemList();
//        List<Task> taskList = timeTable.getTaskList();
//        taskList.forEach(e -> System.out.println(e.getFullTaskName()));
////        Map<ScheduleDate, Map<ResourceItem, List<Task>>> lessonMap = taskList.stream()
////                .filter(task -> task.getScheduleDate() != null && task.getResourceItem() != null)
////                .collect(Collectors.groupingBy(Task::getScheduleDate, Collectors.groupingBy(Task::getResourceItem)));
////        LOGGER.info("|            | " + resourceItemList.stream()
////                .map(resourceItem -> String.format("%-10s", resourceItem.getResourcePoolId())).collect(Collectors.joining(" | ")) + " |");
////        LOGGER.info("|" + "------------|".repeat(resourceItemList.size() + 1));
////        for (ScheduleDate scheduleDate : timeTable.getScheduleDateList()) {
////            List<List<Task>> cellList = resourceItemList.stream()
////                    .map(resourceItem -> {
////                        Map<ResourceItem, List<Task>> byRoomMap = lessonMap.get(scheduleDate);
////                        if (byRoomMap == null) {
////                            return Collections.<Task>emptyList();
////                        }
////                        List<Task> cellTaskList = byRoomMap.get(resourceItem);
////                        return Objects.requireNonNullElse(cellTaskList, Collections.<Task>emptyList());
////                    })
////                    .collect(Collectors.toList());
////
////            LOGGER.info("| " + String.format("%-10s",
////                    scheduleDate.getLocalDateTime().toString().substring(0, 10)) + " | "
////                    + cellList.stream().map(cellLessonList -> String.format("%-10s",
////                            cellLessonList.stream().map(Task::getId).collect(Collectors.joining(" "))))
////                    .collect(Collectors.joining(" | "))
////                    + " |");
////            LOGGER.info("|            | "
////                    + cellList.stream().map(cellLessonList -> String.format("%-10s",
////                            cellLessonList.stream().map(e -> "第0" + e.getLayerNum() + "层").collect(Collectors.joining(", "))))
////                    .collect(Collectors.joining(" | "))
////                    + " |");
//////            LOGGER.info("|            | "
//////                    + cellList.stream().map(cellLessonList -> String.format("%-10s",
//////                            cellLessonList.stream().map(Task::getTaskOrder).collect(Collectors.joining(", "))))
//////                    .collect(Collectors.joining(" | "))
//////                    + " |");
//////            LOGGER.info("| " + String.format("%-10s",
//////                    scheduleDate.getLocalDateTime().toString().substring(0,10) ) + " | "
//////                    + cellList.stream().map(cellLessonList -> String.format("%-10s",
//////                            cellLessonList.stream().map(Task::getTaskOrder).collect(Collectors.joining(", "))))
//////                    .collect(Collectors.joining(" | "))
//////                    + " |");
////            LOGGER.info("|" + "------------|".repeat(resourceItemList.size() + 1));
////        }
////        List<Task> unassignedTasks = taskList.stream()
////                .filter(task -> task.getResourceItem() == null || task.getScheduleDate() == null)
////                .collect(Collectors.toList());
////        if (!unassignedTasks.isEmpty()) {
////            LOGGER.info("");
////            LOGGER.info("Unassigned lessons");
////            for (Task task : unassignedTasks) {
////                LOGGER.info("  " + task.getCode() + " - " + task.getTaskOrder());
////            }
////        }
//    }
//
//}
