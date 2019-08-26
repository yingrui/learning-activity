package me.yingrui.learning.activity.services;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ActivitiService {

    Logger logger = LoggerFactory.getLogger(ActivitiService.class);

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    private final HistoryService historyService;

    private final RepositoryService repositoryService;

    @Autowired
    public ActivitiService(RuntimeService runtimeService, TaskService taskService, HistoryService historyService, RepositoryService repositoryService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
        this.repositoryService = repositoryService;
    }

    public ProcessDefinition deployProcess(MultipartFile bpmn, String path) throws IOException {
        //上传文件到processes
        File file = new File(path + bpmn.getOriginalFilename());
        bpmn.transferTo(file);

        String resource = ResourceLoader.CLASSPATH_URL_PREFIX + bpmn.getOriginalFilename();
        Deployment deployment = repositoryService.createDeployment().addClasspathResource(resource).deploy();
        logger.info("Process [" + deployment.getName() + "] deployed successful");
        return repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
    }

    public ProcessInstance startProcess(String key, Map<String, Object> object) {
        return runtimeService.startProcessInstanceByKey(key, object);
    }

    public ProcessInstance startProcess(String key) {
        return runtimeService.startProcessInstanceByKey(key);
    }

    public List<TaskInfo> getTasksByAssignee(String assignee) {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(assignee).list();
        List<TaskInfo> infos = new ArrayList<>();
        for (Task task : tasks) {
            infos.add(new TaskInfo(task.getId(), task.getName()));
        }
        return infos;
    }

    public List<TaskInfo> getTasksByGroup(String group) {
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup(group).list();
        List<TaskInfo> infos = new ArrayList<>();
        for (Task task : tasks) {
            infos.add(new TaskInfo(task.getId(), task.getName()));
        }
        return infos;
    }

    public List<TaskInfo> getTasks(String assigneeOrGroup) {
        List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(assigneeOrGroup).list();
        List<TaskInfo> infos = new ArrayList<>();
        for (Task task : tasks) {
            infos.add(new TaskInfo(task.getId(), task.getName()));
        }
        return infos;
    }

    public void completeTask(String taskId, Object item) {
        taskService.complete(taskId);
    }

    public void completeTask(String taskId) {
        taskService.complete(taskId);
    }

    public static class TaskInfo {

        private String id;
        private String name;

        public TaskInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }


}
