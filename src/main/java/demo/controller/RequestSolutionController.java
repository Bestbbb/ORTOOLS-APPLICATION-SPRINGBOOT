package demo.controller;

import com.alibaba.fastjson2.JSONObject;
import com.google.ortools.Loader;
import demo.service.AssignedTaskService;
import demo.service.PhaseOneAssignedTaskService;
import demo.service.PhaseThreeAssignedTaskService;
import demo.service.PhaseTwoAssignedTaskService;
import demo.solver.OrToolsJobApp;
import demo.bootstrap.DataGenerator;
import demo.domain.*;
import demo.jsonUtils.LoadFile;
import demo.solver.PhaseOne;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RequestSolutionController {
    @Autowired
    private MesConfig config;
    @Autowired
    private PhaseOneAssignedTaskService phaseOneAssignedTaskService;
    @Autowired
    private PhaseTwoAssignedTaskService phaseTwoAssignedTaskService;
    @Autowired
    private PhaseThreeAssignedTaskService phaseThreeAssignedTaskService;

    @PostMapping("/planner/requestSolution")
    public JSONObject requestSolution(String algorithmFileInputPath,String algorithmFileId){
        System.out.println(config.getUrl());
        JSONObject jsonObject = new JSONObject();
        Input input = LoadFile.readJsonFile(algorithmFileInputPath);
        List<ResourceItem> resourceItems = DataGenerator.generateResources(input);
        List<ManufacturerOrder> orders = DataGenerator.generateOrderList(input);
        List<Task> tasks = DataGenerator.generateTaskList(orders);
        Loader.loadNativeLibraries();
        OrToolsJobApp orToolsJobApp = new OrToolsJobApp();
        orToolsJobApp.setTaskList(tasks);
        orToolsJobApp.setResourceItems(resourceItems);
        orToolsJobApp.setManufacturerOrders(orders);
//        orToolsJobApp.calculateHorizon();
//        orToolsJobApp.generateVariables();
//        orToolsJobApp.createConstraints();
////        orToolsJobApp.createRelatedLayerConstraints();
//        orToolsJobApp.createPrecedence();
//        orToolsJobApp.defineObjective();
        List<PhaseOneAssignedTask> phaseOneAssignedTasks = orToolsJobApp.solvePhaseOne();
        if(phaseOneAssignedTasks==null) {
            jsonObject.put("code", 500);
            jsonObject.put("message", "Error in phase 1");
            return jsonObject;
        }
        phaseOneAssignedTaskService.saveOrUpdateBatch(phaseOneAssignedTasks);
        List<PhaseTwoAssignedTask> phaseTwoAssignedTasks = orToolsJobApp.solvePhaseTwo();
        if(phaseTwoAssignedTasks==null) {
            jsonObject.put("code", 500);
            jsonObject.put("message", "Error in phase 2");
            return jsonObject;
        }
        phaseTwoAssignedTaskService.saveOrUpdateBatch(phaseTwoAssignedTasks);
        List<PhaseThreeAssignedTask> phaseThreeAssignedTasks = orToolsJobApp.solvePhaseThree();
        if(phaseThreeAssignedTasks==null||phaseThreeAssignedTasks.size()==0) {
            jsonObject.put("code", 500);
            jsonObject.put("message", "Error in phase 3");
            return jsonObject;
        }
        phaseThreeAssignedTaskService.saveOrUpdateBatch(phaseThreeAssignedTasks);

        List<AssignedTask> assignedTasks = new ArrayList<>();
        assignedTasks.addAll(phaseOneAssignedTasks);
        assignedTasks.addAll(phaseTwoAssignedTasks);
        assignedTasks.addAll(phaseThreeAssignedTasks);
        orToolsJobApp.setFirstAssignedTasks(assignedTasks);
        orToolsJobApp.output(algorithmFileId);
        jsonObject.put("code",200);
        return jsonObject;
    }

}
