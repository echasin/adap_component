package com.innvo.cucumber.stepdefs;

import com.innvo.AdapcomponentApp;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.boot.test.context.SpringBootTest;

@WebAppConfiguration
@SpringBootTest
@ContextConfiguration(classes = AdapcomponentApp.class)
public abstract class StepDefs {

    protected ResultActions actions;

}
