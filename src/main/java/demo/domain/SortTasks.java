package demo.domain;

import java.util.Comparator;

public class SortTasks implements Comparator<AssignedTask> {

    @Override
    public int compare(AssignedTask o1, AssignedTask o2) {
        if(o1.getStart()!=o2.getStart()){
            return o1.getStart()-o2.getStart();
        }else{
            return o1.getHoursDuration()-o2.getHoursDuration();
        }

    }
}
