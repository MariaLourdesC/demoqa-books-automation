package com.demoqa.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

// Sin filtro fijo: se ejecutan todos los escenarios. Para acotar la corrida se
// usa -Dcucumber.filter.tags="@login_fallido" desde Maven.
@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features", glue = "com.demoqa", monochrome = true)
public class RunCucumberTest {
}
