package com.miaosha.project.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaosha.project.controller.viewobject.UserVo;
import com.miaosha.project.error.BusinessException;
import com.miaosha.project.error.EmBusinessError;
import com.miaosha.project.response.CommonReturnType;
import com.miaosha.project.service.Model.UserModel;
import com.miaosha.project.service.UserService;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")    //用于处理ajax跨域访问问题,但是对ajax不能进行session共享，首先我们需要指定CrossOrigin范围
public class UserController extends BaseController{

    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest httpServletRequest;


    //用户登录接口
    @RequestMapping(value = "/login", method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name="telphone")String telphone,
                                     @RequestParam(name="password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
       //入参校验
        if (org.apache.commons.lang3.StringUtils.isEmpty(telphone))
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);

        //用户登录服务，用来检验用户登录是否合法
        UserModel userModel = userService.validateLogin(telphone, this.EncodeByMD5(password));

        //将登陆凭证加入到用户登陆成功的session内
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        UserVo userVo = convertFormModel(userModel);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userVo);

        return CommonReturnType.create(null);
    }



    //用户注册接口
    @RequestMapping(value = "/register", method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name="telphone")String telphone,
                                     @RequestParam(name="otpCode")String otpCode,
                                     @RequestParam(name="name")String name,
                                     @RequestParam(name="gender") Byte gender,
                                     @RequestParam(name="age")Integer age,
                                     @RequestParam(name="password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号和对应的otpCode相符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        //为什么要使用类库中的equals，因为进行判空处理
        if (!StringUtils.equals(otpCode, inSessionOtpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码错误");
        }
        //用户的注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        //java默认的MD5方式实现的结果只支持16MD5
        userModel.setEncrptPassword(this.EncodeByMD5(password));
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    public String EncodeByMD5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        //加密字符串
        String newStr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return  newStr;
    }

    //用户获取otp短信接口
    @RequestMapping(value = "/getOtp", method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telphone")String telphone){
        //需要按照一定的规则生产OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(999999-100000);
        randomInt += 100000;
        String otpCode = String.valueOf(randomInt);

        //将OTP验证码同时对应用户的手机号关联，使用httpsession的方式绑定他的手机号码与otpCode
        httpServletRequest.getSession().setAttribute(telphone, otpCode);


        //将otp验证码通过短信通道发送给用户，省略
        System.out.println("telphone="+telphone+"&optCode="+otpCode);

        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id")Integer id) throws BusinessException {
        //调用service服务获取对应ID的用户并返回给前端
        UserModel userModel = userService.getUserByID(id);

        //若获取的对应用户信息不存在
        if(userModel ==null) throw new BusinessException(EmBusinessError.USER_NOT_EXIT);

        //将核心领域模型用户对象转化为可供UI使用的viewobject
        UserVo userVo = convertFormModel(userModel);
        //返回通用对象
        return CommonReturnType.create(userVo);
    }

    private UserVo convertFormModel(UserModel userModel){
        if(userModel == null) return null;
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(userModel, userVo);
        return userVo;
    }
}
