package com.example.bootutil.interceptor;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表单防重注解 只可生效在已登录的方法上【默认防重时间 5000毫秒 】
 * <p>
 * @author cg
 * @version 1.0
 * @date 2021/6/15 11:17
 *
 * @Description
 * 不登录 （token不存在） 放行不进行限制不做校验（原因：所有用户请求都进行同一限制） 请注意，此种情况即使增加注解也不生效
 * 登录 （token存在）
 *    限制规则
 *      前缀 +   MD5Util#MD5Encode（ token + currentSystem值 + 请求路径）
 *
 *  在使用过程中你需要考虑方法执行时间和 Redis的处理命令所需时间 ，俩者总和去设置【intervalTime】该参数
 *
 * 【使用示例】
 *        默认:                           @RepeatForm
 *        俩系统出现相同请求路径:            @RepeatForm(currentSystem = "oms")
 *        俩系统出现相同请求路径并执行时长:    @RepeatForm(currentSystem = "oms",intervalTime = 20000)
 *        方法不在进行校验:                 @RepeatForm(required = false,currentSystem = "oms",intervalTime = 20000)  /直接删除该注解
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatForm {

    //是否校验 true 校验  false 不校验放行
    boolean required() default true;

    //防重提交 间隔时间( 单位 毫秒)  设置毫秒为单位的原因：减少拦截器中的时间计算
    long intervalTime() default 5000;

    //所属系统 进行防止重复接口，默认不需要，如出现不同系统相同路径的情况 增加该参数进行区分 ,
    String currentSystem() default "";

    //描述 日志分析使用
    String remark() default "";


}
