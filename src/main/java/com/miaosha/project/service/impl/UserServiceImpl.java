package com.miaosha.project.service.impl;

import com.miaosha.project.dao.UserDoMapper;
import com.miaosha.project.dao.UserPasswordDoMapper;
import com.miaosha.project.dataobject.UserDo;
import com.miaosha.project.dataobject.UserPasswordDo;
import com.miaosha.project.error.BusinessException;
import com.miaosha.project.error.EmBusinessError;
import com.miaosha.project.service.Model.UserModel;
import com.miaosha.project.service.UserService;
import com.miaosha.project.validator.ValidationResult;
import com.miaosha.project.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDoMapper userDoMapper;

    @Autowired
    private UserPasswordDoMapper userPasswordDoMapper;

    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel validateLogin(String telphone, String encrptpassword) throws BusinessException {
        //通过用户的手机号获取用户信息
        UserDo userDo = userDoMapper.selectByTelPhone(telphone);
        if (userDo == null) throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        UserPasswordDo userPasswordDo = userPasswordDoMapper.selectByUserId(userDo.getId());

        UserModel userModel = convertFromDataObject(userDo, userPasswordDo);

        //比对用户信息内加密的密码是否和传输进来的密码相匹配
        if (!StringUtils.equals(encrptpassword, userModel.getEncrptPassword()))
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);

        return userModel;

    }

    @Override
    public UserModel getUserByID(Integer id) {
        //调用userDoMapper获取到对应的用户dataobject
        UserDo userDo = userDoMapper.selectByPrimaryKey(id);
        if(userDo == null) return null;
        //通过用户id获取对应的用户加密密码信息
        UserPasswordDo userPasswordDo = userPasswordDoMapper.selectByPrimaryKey(userDo.getId());

        return convertFromDataObject(userDo, userPasswordDo);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
      /*  if (StringUtils.isEmpty(userModel.getName())
            || userModel.getGender() == null
            || userModel.getAge() == null
            || StringUtils.isEmpty(userModel.getTelphone()))
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);*/

        //使用validator的方式进行校验
        ValidationResult result = validator.validate(userModel);
        if (result.isHasErrors())
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());

        /*为什么使用insertSelective而不用insert，因为insertSelective会判断字段是否为null，
        如果为null则不往数据库里添加，使用数据库中的默认值
        而且对于update更加有效，因为为空的化表示不修改
        数据库设计尽量使用not null因为java处理空指针很弱，设计数据库的时候给他默认值*/
        UserDo userDo = convertFromModel(userModel);
        try{
            userDoMapper.insertSelective(userDo);
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"该手机号已注册");
        }

        userModel.setId(userDo.getId());

        UserPasswordDo userPasswordDo = convertPasswardFromModel(userModel);
        userPasswordDoMapper.insertSelective(userPasswordDo);

        return;
    }

    /**
     * 将UserModel转成UserPasswordDo
     * @param userModel
     * @return
     */
    private UserPasswordDo convertPasswardFromModel(UserModel userModel){
        if (userModel == null) return null;
        UserPasswordDo userPasswordDo = new UserPasswordDo();
        userPasswordDo.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDo.setUserId(userModel.getId());
        return userPasswordDo;
    }

    /**
     * 将UserModel转成UserDo
     * @param userModel
     * @return
     */
    private UserDo convertFromModel(UserModel userModel){
        if (userModel == null) return null;
        UserDo userDo = new UserDo();
        BeanUtils.copyProperties(userModel, userDo);
        return userDo;
    }

    /**
     * UserDo和UserPasswordDo 转成DataObject
     * @param userDo
     * @param userPasswordDo
     * @return
     */
    private UserModel convertFromDataObject(UserDo userDo, UserPasswordDo userPasswordDo){
        if(userDo == null) return  null;
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDo, userModel);
        if(userPasswordDo == null) return null;
        userModel.setEncrptPassword(userPasswordDo.getEncrptPassword());
        return  userModel;
    }
}
