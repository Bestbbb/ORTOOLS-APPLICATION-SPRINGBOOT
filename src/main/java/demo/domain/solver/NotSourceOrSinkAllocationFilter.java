package demo.domain.solver;//package demo.domain.solver;
//
//
//import com.example.optaplanner_industry.demo.domain.Allocation;
//import com.example.optaplanner_industry.demo.domain.Schedule;
//import com.example.optaplanner_industry.demo.domain.TaskType;
//import org.optaplanner.core.api.domain.entity.PinningFilter;
//
//public class NotSourceOrSinkAllocationFilter implements PinningFilter<Schedule, Allocation> {
//
//    @Override
//    public boolean accept(Schedule schedule, Allocation allocation) {
//        TaskType taskType = allocation.getTask().getTaskType();
//        return taskType == TaskType.SOURCE || taskType == TaskType.SINK;
//    }
//
//}
