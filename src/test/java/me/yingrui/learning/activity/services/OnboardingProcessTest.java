package me.yingrui.learning.activity.services;

import me.yingrui.learning.activity.Application;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.LongFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
/**
 * https://www.activiti.org/quick-start
 */
public class OnboardingProcessTest {

    @Autowired
    ActivitiService activitiService;

    @Autowired
    FormService formService;

    @Autowired
    TaskService taskService;

    @Autowired
    HistoryService historyService;

    @Autowired
    RuntimeService runtimeService;

    @Test
    public void should_finish_process_for_managers() {
        ProcessInstance process = startTask("onboarding");
        assertNotNull(process);

        while (process != null && !process.isEnded()) {
            List<ActivitiService.TaskInfo> tasks = activitiService.getTasksByGroup("managers");
            System.out.println();
            System.out.println("Active outstanding tasks: [" + tasks.size() + "]");

            for (int i = 0; i < tasks.size(); i++) {
                ActivitiService.TaskInfo task = tasks.get(i);
                System.out.println("Processing Task [" + task.getName() + "], Id: " + task.getId());

                Map<String, Object> variables = generateTaskVariables(task);
                taskService.complete(task.getId(), variables);
            }

            printProcessStatus(process);

            process = runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).singleResult();
        }
    }

    private void printProcessStatus(ProcessInstance process) {
        HistoricActivityInstance endActivity = null;
        List<HistoricActivityInstance> activities =
                historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(process.getId()).finished()
                        .orderByHistoricActivityInstanceEndTime().asc()
                        .list();
        for (HistoricActivityInstance activity : activities) {
            if (activity.getActivityType().equals("startEvent")) {
                System.out.println("BEGIN " + process.getProcessDefinitionName()
                        + " [" + process.getProcessDefinitionKey()
                        + "] " + activity.getStartTime());
            }
            if (activity.getActivityType().equals("endEvent")) {
                // Handle edge case where end step happens so fast that the end step
                // and previous step(s) are sorted the same. So, cache the end step
                //and display it last to represent the logical sequence.
                endActivity = activity;
            } else {
                System.out.println("-- " + activity.getActivityName()
                        + " [" + activity.getActivityId() + "] "
                        + activity.getDurationInMillis() + " ms");
            }
        }
        if (endActivity != null) {
            System.out.println("-- " + endActivity.getActivityName()
                    + " [" + endActivity.getActivityId() + "] "
                    + endActivity.getDurationInMillis() + " ms");
            System.out.println("COMPLETE " + process.getProcessDefinitionName() + " ["
                    + process.getProcessDefinitionKey() + "] "
                    + endActivity.getEndTime());
        }
    }

    private Map<String, Object> generateTaskVariables(ActivitiService.TaskInfo task) {
        Random random = new Random(System.currentTimeMillis());
        Map<String, Object> variables = new HashMap<>();
        FormData formData = formService.getTaskFormData(task.getId());
        for (FormProperty formProperty : formData.getFormProperties()) {
            if (StringFormType.class.isInstance(formProperty.getType())) {
                String value = UUID.randomUUID().toString();
                System.out.println(formProperty.getName() + "? " + value);
                variables.put(formProperty.getId(), value);
            } else if (LongFormType.class.isInstance(formProperty.getType())) {
                Long value = new Long(random.nextInt(20));
                System.out.println(formProperty.getName() + "? " + value);
                variables.put(formProperty.getId(), value);
            } else if (DateFormType.class.isInstance(formProperty.getType())) {
                Date value = new Date();
                System.out.println(formProperty.getName() + "? " + value);
                variables.put(formProperty.getId(), value);
            } else {
                System.out.println("<form type not supported>");
            }
        }
        return variables;
    }

    private ProcessInstance startTask(String key) {
        return activitiService.startProcess(key);
    }

}