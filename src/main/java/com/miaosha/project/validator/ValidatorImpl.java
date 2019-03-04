package com.miaosha.project.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;


/**
 * 具体的validator
 */
@Component  //是一个spring的bean
public class ValidatorImpl implements InitializingBean {

    //java中validator工具类
    private Validator validator;

    /**
     * 实现校验方法并返回校验结果
     * @param bean  可以是任何的bean
     * @return
     */
    public ValidationResult validate(Object bean){
        final ValidationResult validationResult = new ValidationResult();
        //若对应的bean的参数有违背了validator的校验规则，set中就会有对应的值
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if (constraintViolationSet.size() > 0){
            //有错误
            validationResult.setHasErrors(true);
            //遍历错误信息
            constraintViolationSet.forEach(constraintViolation->{
                String errMsg = constraintViolation.getMessage();
                String propertyName = constraintViolation.getPropertyPath().toString();
                validationResult.getErrorMsgMap().put(propertyName, errMsg);
            });
        }
        return validationResult;
    }

    /**
     * 当我们的springbean初始化完成之后
     * 会回调ValidatorImpl中afterPropertiesSet方式
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //将hibernate valieator通过工厂初始化方式使其实例化
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
