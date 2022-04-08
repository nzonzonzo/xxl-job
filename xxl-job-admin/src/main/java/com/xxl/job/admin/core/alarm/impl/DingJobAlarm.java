package com.xxl.job.admin.core.alarm.impl;

import com.xxl.job.admin.core.alarm.JobAlarm;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 钉钉告警
 *
 * @author
 */
@Component
public class DingJobAlarm implements JobAlarm {

    private static Logger logger = LoggerFactory.getLogger(DingJobAlarm.class);

    private String baseDingWebhook = "https://oapi.dingtalk.com/robot/send?";

    @Autowired
    RestTemplate restTemplate;

    /**
     * fail alarm
     *
     * @param jobLog
     */

    @Override
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog){
        boolean alarmResult = true;
        // send monitor email
        if (info!=null && info.getAlarmEmail()!=null && info.getAlarmEmail().trim().length()>0) {

            if (!info.getAlarmEmail().contains("access_token")){
                return alarmResult;
            }
            Set<String> dingWebhookSet = new HashSet<String>(Arrays.asList(info.getAlarmEmail().split(",")));

            Map<String, Object> map = loadEmailJobAlarmTemplate(info,jobLog);
            // 发送钉钉消息
            for (String dingWebhook: dingWebhookSet) {

                try {

                    restTemplate.postForEntity(baseDingWebhook + dingWebhook, map, Object.class);

                } catch (Exception e) {
                    logger.error(">>>>>>>>>>> xxl-job, job fail alarm email send error, JobLogId:{}", jobLog.getId(), e);

                    alarmResult = false;
                }

            }

        }

        return alarmResult;
    }


    private static final Map<String, Object> loadEmailJobAlarmTemplate(XxlJobInfo info, XxlJobLog jobLog){
        HashMap<String, Object> map = new HashMap<>(2);
        /** 设置消息类型 **/
        map.put("msgtype", "text");

        /** 设置消息内容 -- start **/
        String msg = "<br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>";
        String TrimTriggerMsg = jobLog.getTriggerMsg().replace(msg,"");
        String TriggerMsg = TrimTriggerMsg.replace("<br>", "\n");
        String content =
                "【告警信息】 \t\n" +
                        "负责人 : \t" + info.getAuthor() + "\t\n" +
                        "任务id : \t" + info.getId() + "\t\n" +
                        "任务名称 : \t" + info.getJobDesc() + "\t\n" +
                        "执行器名称 : \t" + info.getExecutorHandler() + "\t\n" +
                        "执行器ip : \t" + jobLog.getExecutorAddress() + "\t\n" +
                        "任务参数 : \t" + jobLog.getExecutorParam() + "\t\n" +
                        "LogId : \t" + jobLog.getId() + "\t\n" +
                        "TriggerMsg : \t" + TriggerMsg + "\t\n" +
                        //jobLog.getTriggerMsg().replace("<br>","\n")
                        "HandleCode : \t" + jobLog.getHandleMsg() + "\t\n" +
                        "报警时间 : \t" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\t\n";

        HashMap<String, String> cmap = new HashMap<>(1);
        cmap.put("content",content);
        map.put("text", cmap);
        /** 设置消息内容 -- stop **/



        /** 设置是否@指定人 --start **/
        Map<String, Object> atmap = new HashMap<String, Object>();
        String[] authorList = info.getAuthor().split(",");
        ArrayList arrayList = new ArrayList();
        for (String author : authorList){
            if ("".equals(author) || author.split("-").length<2){
                continue;
            }
            arrayList.add(author.split("-")[1]);
        }
        if (arrayList.size() > 0){
            atmap.put("atMobiles",arrayList.toArray());
            map.put("at",atmap);
        }
        /** 设置是否@指定人 --stop **/

        return map;
    }

}
