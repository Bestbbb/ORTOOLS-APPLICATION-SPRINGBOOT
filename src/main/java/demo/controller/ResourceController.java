package demo.controller;

import demo.domain.*;
import demo.jsonUtils.LoadFile;
import demo.vo.ResourceVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ResourceController {

    @PostMapping("/getResourceList")
    public List<String> getResourceList(String algorithmFileOutputPath) {
        Input input = LoadFile.readJsonFile(algorithmFileOutputPath);
        List<ResourcePool> resourcePool = input.getResourcePool();
        List<String> collect = resourcePool.stream().map(i->{
            String typeId = i.getTypeId();
            String code = i.getCode();
            return typeId+" "+code;
        }).collect(Collectors.toList());
        return collect;
    }


    @PostMapping("/getResourceRunningTime")
    public List<ResourceVo> getResourceRunningTime(String algorithmFileOutputPath) {
        List<ResourceVo> resourceVos = new ArrayList<>();
        Input input = LoadFile.readJsonFile(algorithmFileOutputPath);
        List<ManufacturerOrder> orderList = input.getManufacturerOrderList();
        for(ManufacturerOrder order:orderList){
            List<Step> stepList = order.getProduct().getStepList();
            for(Step step:stepList){
                long executionDays = step.getExecutionDays();
                LocalDate stepStartTime = step.getStepStartTime();
                LocalDate endTime = stepStartTime.plusDays(executionDays);
                String resourceId = step.getResourceRequirementList().get(0).getResourceId();
                String id = step.getResourceRequirementList().get(0).getId();
                ResourceVo resourceVo = new ResourceVo();
                HashMap<String,Integer> dateToHoursPerDay = new HashMap<>();
                resourceVo.setId(id);
                resourceVo.setResourceId(resourceId);
                resourceVo.setStartTime(stepStartTime.toString());
                resourceVo.setExecutionDays(executionDays);
                resourceVo.setEndTime(endTime.toString());
                resourceVos.add(resourceVo);

            }
        }
        List<ResourceVo> collect = resourceVos.stream().sorted((o1, o2) -> {
            if (o1.getStartTime().compareTo(o2.getStartTime()) > 0)
                return 1;
            if (o1.getStartTime().compareTo(o2.getStartTime()) < 0)
                return -1;
            return 0;
        }).collect(Collectors.toList());
        return collect;

    }



    @PostMapping("/getResourceStatus")
    public HashMap<LocalDate, Map<String, Long>> getResourceStatus(String algorithmFileOutputPath) {
        Input input = LoadFile.readJsonFile(algorithmFileOutputPath);
        return input.getDateToResourceIdToHoursPerDay();
    }

}
