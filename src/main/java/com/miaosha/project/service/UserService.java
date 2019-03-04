package com.miaosha.project.service;

import com.miaosha.project.error.BusinessException;
import com.miaosha.project.service.Model.UserModel;

public interface UserService {
    UserModel getUserByID(Integer id);
    void register(UserModel userModel) throws BusinessException;

    /**
     *
     * @param telphone  用户注册手机
     * @param password  用户加密后的密码
     * @throws BusinessException
     */
    UserModel validateLogin(String telphone, String encrptpassword) throws BusinessException;
}
