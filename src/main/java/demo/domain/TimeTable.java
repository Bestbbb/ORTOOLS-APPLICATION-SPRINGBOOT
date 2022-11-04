package demo.domain;//package com.example.optaplanner_industry.demo.domain;
//
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
//import org.optaplanner.core.api.domain.solution.PlanningScore;
//import org.optaplanner.core.api.domain.solution.PlanningSolution;
//import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
//import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
//import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
//
//import java.util.List;
//
//@Data
//@NoArgsConstructor
//@PlanningSolution
//public class TimeTable {
//
//    @ProblemFactCollectionProperty
//    @ValueRangeProvider(id = "scheduleRange")
//    private List<ScheduleDate> scheduleDateList;
//    @ProblemFactCollectionProperty
//    @ValueRangeProvider(id = "resourceRange")
//    private List<ResourceItem> resourceItemList;
//    @PlanningEntityCollectionProperty
//    @ValueRangeProvider(id = "taskRange")
//    private List<Task> taskList;
//
//    @PlanningScore
//    private HardSoftScore score;
//
//    public TimeTable(List<ScheduleDate> scheduleDateList, List<ResourceItem> resourceItemList, List<Task> taskList) {
//        this.scheduleDateList = scheduleDateList;
//        this.resourceItemList = resourceItemList;
//        this.taskList = taskList;
//    }
//
//}
