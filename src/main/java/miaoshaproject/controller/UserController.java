package miaoshaproject.controller;

import miaoshaproject.controller.viewobject.UserVO;
import miaoshaproject.error.BusinessException;
import miaoshaproject.error.EmBusinessError;
import miaoshaproject.response.CommonReturnType;
import miaoshaproject.service.UserService;
import miaoshaproject.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",origins = {"*"},allowedHeaders = {"*"})
public class UserController extends BaseController{
    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest httpServletRequest;

    //用户登录接口
    @RequestMapping(value="/login",method = RequestMethod.POST,consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType login(@RequestParam(name="telphone")String telphone,
                                  @RequestParam(name = "password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if(org.apache.commons.lang3.StringUtils.isEmpty(telphone)|| StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //用户登录服务，校验用户登录是否合法
        UserModel userModel=userService.validateLogin(telphone,this.EncodeByMd5(password));
        //登录凭证加入用户session内
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);
        return CommonReturnType.create(null);
    }
    //用户注册接口
    @RequestMapping(value="/register",method = RequestMethod.POST,consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone")String telphone,
                                     @RequestParam(name = "otpCode")String otpCode,
                                     @RequestParam(name = "name")String name,
                                     @RequestParam(name = "gender")Integer gender,
                                     @RequestParam(name = "age")Integer age,
                                     @RequestParam(name = "password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //前端路径千万不要用绝对路径
        String inSessionOtpCode= (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if(!com.alibaba.druid.util.StringUtils.equals(otpCode,inSessionOtpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符合");
        }
        UserModel userModel=new UserModel();
        userModel.setEncrptPassword(this.EncodeByMd5(password));
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelephone(telphone);
        userModel.setRegisterMode("byphone");

        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    public String EncodeByMd5(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest md5=MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();

        String newstr=base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }

    //用户获取otp短信接口
    @RequestMapping(value="/getotp",method = RequestMethod.POST,consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name="telphone")String telphone){
        //生成OTP验证码
        Random random=new Random();
        int randomInt=random.nextInt(99999);
        randomInt+=10000;
        String otpCode=String.valueOf(randomInt);
        //将OTP与手机号关联
        httpServletRequest.getSession().setAttribute(telphone,otpCode);
        //Object a=httpServletRequest.getSession();
        //将OTP发给用户
        System.out.println("telphone="+telphone+"&otpCode="+otpCode);
        return CommonReturnType.create(null);
    }

    //得到用户信息
    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id")Integer id) throws BusinessException {
        //调用service服务获取id的用户对象返回前端
        UserModel userModel=userService.getUserById(id);
        //用户信息不存在
        if(userModel==null){
            userModel.setEncrptPassword("123");
            //throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        //将核心领域用户对象转成viewobject
        UserVO userVO = convertFormModel(userModel);

        return CommonReturnType.create(userVO);
    }

    private UserVO convertFormModel(UserModel userModel){
        if (userModel==null){
            return null;
        }
        UserVO userVO =new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }

}
