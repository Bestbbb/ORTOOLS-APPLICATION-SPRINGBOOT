package demo.domain;

import lombok.Data;

@Data
public abstract class TaskOrResource {

    protected Task nextTask;

    public abstract Integer getEndTime(int quantity);

}
