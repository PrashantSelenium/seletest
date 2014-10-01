/*
This file is part of the Seletest by Papadakis Giannis <gpapadakis84@gmail.com>.

Copyright (c) 2014, Papadakis Giannis <gpapadakis84@gmail.com>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.automation.seletest.core.listeners;


import java.io.File;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.annotation.AnnotationUtils;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import com.automation.seletest.core.selenium.configuration.SessionControl;

import com.automation.seletest.core.selenium.threads.SessionContext;
import com.automation.seletest.core.services.PerformanceUtils;
import com.automation.seletest.core.services.annotations.SeleniumTest;

@Slf4j
public class InitListener implements IInvokedMethodListener{

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {

        log.debug("Specify browser type by finding custom annotation on class level!!!");
        SeleniumTest seleniumTest=AnnotationUtils.findAnnotation(method.getTestMethod().getTestClass().getRealClass(), SeleniumTest.class);

        //Set assertion type (Hard / Soft) for this test method
        if(method.getTestMethod().isTest()){
            seleniumTest=AnnotationUtils.findAnnotation(method.getTestMethod().getConstructorOrMethod().getMethod(), SeleniumTest.class);
            SessionControl.verifyController().setAssertionType(seleniumTest.assertion());
        }

    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if(method.getTestMethod().isTest()){
            PerformanceUtils perf=(PerformanceUtils) SessionContext.getSession().getControllers().get(PerformanceUtils.class);
            SessionControl.verifyController().assertAll();

            //Performance collection data
            if(perf!=null){
                perf.getPerformanceData(perf.getServer());
                perf.writePerformanceData(new File("./target/surefire-reports/logs/"+testResult.getName()+".har").getAbsolutePath(), perf.getHar());
                perf.stopServer(perf.getServer());
                log.info("Performance data collected for test: {} !!!",testResult.getName());
            }
        }
    }
}