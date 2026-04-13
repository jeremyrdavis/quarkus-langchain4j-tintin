package com.redhat.tintin.tools;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Year;

@ApplicationScoped
public class DateCalculatorTool {

    @Tool("Calculate how many years ago a given year was")
    public int yearsAgo(int year) {
        return Year.now().getValue() - year;
    }

    @Tool("Calculate the time span in years between two given years")
    public int yearsBetween(int startYear, int endYear) {
        return endYear - startYear;
    }
}
