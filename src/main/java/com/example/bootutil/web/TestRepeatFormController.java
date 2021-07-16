package com.example.bootutil.web;

import com.example.bootutil.interceptor.RepeatForm;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cg
 * @version 1.0
 * @date 2021/6/15 11:16
 *
 * 表单防重提交测试
 *   原理，通过增加注解，利用spring可扩展拦截器机制进行使用 redis防重提交
 */
@RestController
public class TestRepeatFormController {


    @RequestMapping(value = "/test")
    public String test(){
        return "test";
    }


    /**
     * @Author chengeng
     * @Description 测试表单防重
     * @Date 17:21 2021/6/15
     * @Param []
     * @return java.lang.String
     **/
    @RequestMapping(value = "/testForm")
    @RepeatForm(remark = "123",intervalTime = 20000)
    public String test1() throws InterruptedException {
        long begin = System.currentTimeMillis();

        //Thread.sleep(6000);
        long endTime = System.currentTimeMillis();
        System.out.println("执行方法时间戳时间"+(endTime-begin));
        return "test1";
    }
}
