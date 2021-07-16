package com.example.bootutil.interceptor;

import com.example.bootutil.config.RedisDistributedLock;
import com.example.bootutil.exception.ResponseException;
import com.example.bootutil.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;


/**
 * @author cg
 * @version 1.0
 * @date 2021/6/15 16:06
 * 表单防重拦截器
 * 经测试 拦截截器逻辑处理时间第一次700毫秒左右、之后每次请求 2毫秒 的执行时间，未考虑Redis执行时长， 原因：拦截器属于是懒加载
 */
public class PreventionRepeatInterceptor implements HandlerInterceptor {

    /**
     * @Author chengeng
     * @Description 定义Redis 前缀
     * @Date 10:41 2021/6/21
     * @Param
     * @return
     **/
    private static final String   PREFIX_INTERFACE_REPEAT = "prefix_interface_repeat_";

    Logger log = LoggerFactory.getLogger(PreventionRepeatInterceptor.class);


    @Autowired
    private RedisDistributedLock distributedLock;



    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {

        // 获取token 请求值
        String token = httpServletRequest.getHeader("token");


        if (StringUtils.isEmpty(token)){
           return true;
        }

        // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) object;
        Method method = handlerMethod.getMethod();
        //检查是否有RepeatForm注释，不存在则跳过认证
        if (!method.isAnnotationPresent(RepeatForm.class)) {
            return true;
        }
        //获取注解信息
        RepeatForm repeatForm = method.getAnnotation(RepeatForm.class);
        if (!repeatForm.required()) {
            return true;
        }

        // 获取请求路径
        String requestUrl = httpServletRequest.getServletPath();

        //加密后获取缩短key存储路径 【前缀 + MD5加密值】
        StringBuffer lockKey = new StringBuffer(PREFIX_INTERFACE_REPEAT + MD5Util.MD5Encode(token + repeatForm.currentSystem() + requestUrl));

        //设置方法执行起始时间
        httpServletRequest.setAttribute("beginTime",System.currentTimeMillis());

        log.debug("***表单防重-key:{}; requestUrl:{}; method:{}; remark:{}***",  lockKey,requestUrl,method.getName(),repeatForm.remark());
        if (!distributedLock.lock(lockKey.toString(), repeatForm.intervalTime() )) {
            log.error("***表单防重-重复提交-key:{}; requestUrl:{}; method:{}; 超时时间限制:{} 毫秒,remark:{}***",  lockKey,requestUrl,method.getName(),repeatForm.intervalTime(),repeatForm.remark());
            throw new ResponseException("请勿重复提交");
        }
        httpServletRequest.setAttribute("lockKey",lockKey.toString());
        httpServletRequest.setAttribute("intervalTime",repeatForm.intervalTime());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object o, Exception e) throws Exception {

        //获取请求头中设置的信息 ,任意信息不存在则不执行
        String lockKey = (String) httpServletRequest.getAttribute("lockKey");
        Long beginTime = (Long) httpServletRequest.getAttribute("beginTime");
        Long repeatFormIntervalTime = (Long) httpServletRequest.getAttribute("intervalTime");

        if (null == lockKey || null == beginTime || null == repeatFormIntervalTime ) {
            return;
        }

        //获取执行方法时间戳
        Long interval = System.currentTimeMillis() - beginTime;

        //当方法执行完成，超时时间还没过期则手动移除分布式锁
       if (interval < repeatFormIntervalTime){
           //如出现异常则以超时时间来处理
           distributedLock.releaseLockUnsafe(lockKey);
        }
        //当方法执行时间超过设置防重时间 日志则按异常输出
        if (interval >= repeatFormIntervalTime ){
            log.error("请求路径:{};  防重间隔时间设置:{} ; 执行时间:{} (单位/毫秒);",httpServletRequest.getServletPath(),repeatFormIntervalTime,interval);
        }
    }
}
