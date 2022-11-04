package demo.solver;///*
// * Copyright 2020 Red Hat, Inc. and/or its affiliates.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.example.optaplanner_industry.demo.solver;
//
//import com.example.optaplanner_industry.demo.domain.Task;
//import com.example.optaplanner_industry.demo.domain.TaskOrResource;
//import com.example.optaplanner_industry.demo.domain.TimeTable;
//import org.optaplanner.core.api.domain.variable.VariableListener;
//import org.optaplanner.core.api.score.director.ScoreDirector;
//
//import java.util.Objects;
//
//
////每次,有Task对象的previousTaskOrEmployee的property发生更改后,这个监听类就会监听到,然后执行相应操作
//public class StartTimeUpdatingVariableListener implements VariableListener<TimeTable, Task> {
//
//    @Override
//    public void beforeEntityAdded(ScoreDirector<TimeTable> scoreDirector, Task task) {
//        // Do nothing
//    }
//
//    @Override
//    public void afterEntityAdded(ScoreDirector<TimeTable> scoreDirector, Task task) {
//        updateStartTime(scoreDirector, task);
//    }
//
//    @Override
//    public void beforeVariableChanged(ScoreDirector<TimeTable> scoreDirector, Task task) {
//        // Do nothing
//    }
//
//    @Override
//    public void afterVariableChanged(ScoreDirector<TimeTable> scoreDirector, Task task) {
//        updateStartTime(scoreDirector, task);
//    }
//
//    @Override
//    public void beforeEntityRemoved(ScoreDirector<TimeTable> scoreDirector, Task task) {
//        // Do nothing
//    }
//
//    @Override
//    public void afterEntityRemoved(ScoreDirector<TimeTable> scoreDirector, Task task) {
//        // Do nothing
//    }
//
//    protected void updateStartTime(ScoreDirector<TimeTable> scoreDirector, Task sourceTask) {
//        //首先取得该Task对象的前一个实体或锚
//        TaskOrResource previous = sourceTask.getPreviousTaskOrResource();
//        //取得当前Task对象
//        Task shadowTask = sourceTask;
//        //取得前面的截止时间
//        //刚开始的时候，前面是没有的，所以就是null
//        Integer previousEndTime = (previous == null ? null : previous.getEndTime(150));
//        //开始时间取，前面的截止时间和当前任务的准备时间的最大值，因为在这里认为task的准备过程不需要算在任务里面，所以取的较大值，如果是认为需要算在任务时间里面，那么就需要相加了
//        //刚开始的时候，previousEndTime是null，所以startTime也是null
//        Integer startTime = calculateStartTime(shadowTask, previousEndTime);
//
//        //当用来遍历task不是null并且shadowTask的startTime和前面计算的startTime不相等的时候，循环
//        //用来遍历的shadowTask一直向后取，一直取到后面为null的时候，也就是previousTaskOrEmployee属性变更的task后面的所有task的startTime全部变更完毕的时候循环停止
//        //刚开始的时候，previousEndTime和startTime都是null，所以相等，这种情况就不用更新了，然后后面成链的时候这里才比较有用
//        //还有一种就是，两个任务的持续时间一样，这样的话两个任务交换以后后面的任务的时间就不用更新了
//        while (shadowTask != null && !Objects.equals(shadowTask.getStartTime(), startTime)) {
//            //这个方法不知道干了什么
//            scoreDirector.beforeVariableChanged(shadowTask, "startTime");
//            //设置开始时间为刚计算出来的startTime
//            shadowTask.setStartTime(startTime);
//            //这个函数不知道干了什么
//            scoreDirector.afterVariableChanged(shadowTask, "startTime");
//            //前面的截止时间设置为当前任务的截止时间
//            previousEndTime = shadowTask.getEndTime(150);
//            //shadowTask继续向后取task
//            shadowTask = shadowTask.getNextTask();
//            //开始时间取，前面的截止时间和当前任务的准备时间的最大值
//            startTime = calculateStartTime(shadowTask, previousEndTime);
//        }
//    }
//
//    //开始时间取，前面的截止时间和当前任务的准备时间的最大值
//    private Integer calculateStartTime(Task task, Integer previousEndTime) {
//        if (task == null || previousEndTime == null) {
//            return null;
//        }
//        return Math.max(task.getReadyTime(), previousEndTime);
//    }
//
//}
