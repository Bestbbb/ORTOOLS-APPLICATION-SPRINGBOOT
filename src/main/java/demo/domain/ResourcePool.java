package demo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourcePool {

    private String id;
    private String code;
    private String typeId;
    private List<ResourceItem> availableList;

}
