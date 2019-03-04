package com.miaosha.project.controller.viewobject;

/**
 * 视图模型
 */
public class UserVo {
    private  Integer id;
    private  String name;
    private  Byte gender;
    private  Integer age;
    private  String telphone;

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGender(Byte gender) {
        this.gender = gender;
    }


    public void setTelphone(String telphone) {
        this.telphone = telphone;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Byte getGender() {
        return gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getTelphone() {
        return telphone;
    }
}
