package me.yingrui.learning.activity.services;

import com.google.common.collect.ImmutableMap;
import me.yingrui.learning.activity.Application;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class ActivitiServiceTest {

    @Autowired
    ActivitiService activitiService;

    @Test
    public void should_start_a_hello_world_process() {
        Map<String, Object> variables = ImmutableMap.of("email", "john.doe@activiti.com");
        ProcessInstance process = activitiService.startProcess("hello-world", variables);
        Date startTime = process.getStartTime();
        assertNotNull(startTime);
        assertTrue(process.isEnded());
    }

}