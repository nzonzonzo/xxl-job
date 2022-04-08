package com.xxl.job.executor.service.jobhandler;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class MyJobHandler{

    @XxlJob(value = "nzo")
    public ReturnT<String> execute() throws Exception {
        System.out.println("恩佐真的强呀");
        return ReturnT.SUCCESS;
    }
}
