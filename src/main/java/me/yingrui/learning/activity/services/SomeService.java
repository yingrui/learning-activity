package me.yingrui.learning.activity.services;

import org.springframework.stereotype.Service;

@Service
/**
 * An example: <br/>
 * <p>invoke a service in 'Hello World' process.</p>
 */
public class SomeService {

    public void doSomething(String email) {
        System.out.println("doing something... " + email);
    }
}

