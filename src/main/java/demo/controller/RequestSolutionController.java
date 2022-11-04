package demo.controller;

import com.alibaba.fastjson2.JSONObject;
import com.google.ortools.Loader;
import demo.solver.OrToolsJobApp;
import demo.bootstrap.DataGenerator;
import demo.domain.*;
import demo.jsonUtils.LoadFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RequestSolutionController {
    @Autowired
    private MesConfig config;

    @PostMapping("/planner/requestSolution")
    public JSONObject requestSolution(String algorithmFileInputPath){
        System.out.println(config.getUrl());
        JSONObject jsonObject = new JSONObject();
        Input input = LoadFile.readJsonFile(algorithmFileInputPath);
        List<Task> tasks = DataGenerator.generateTaskList(input);
        List<ResourceItem> resourceItems = DataGenerator.generateResources(input);
        List<ManufacturerOrder> orders = DataGenerator.generateOrderList(input);
        Loader.loadNativeLibraries();
        OrToolsJobApp orToolsJobApp = new OrToolsJobApp();
        orToolsJobApp.setTaskList(tasks);
        orToolsJobApp.setResourceItems(resourceItems);
        orToolsJobApp.setManufacturerOrders(orders);
        orToolsJobApp.calculateHorizon();
        orToolsJobApp.generateVariables();
        orToolsJobApp.createConstraints();
//        orToolsJobApp.createRelatedLayerConstraints();
        orToolsJobApp.createPrecedence();
        orToolsJobApp.defineObjective();
        orToolsJobApp.solve();
        orToolsJobApp.output();
        jsonObject.put("code",200);
        return jsonObject;
    }

}
