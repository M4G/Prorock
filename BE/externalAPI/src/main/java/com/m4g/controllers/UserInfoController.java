package com.m4g.controllers;

import com.m4g.dtos.UserInfo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * Created by MaxG on 17-Apr-15.
 */

@RestController
public class UserInfoController {


    @RequestMapping(value = "/userInfo/{sid}", method = RequestMethod.GET)
    public UserInfo getUserInfo(@PathVariable String sid){
        UserInfo retVal = new UserInfo();

        retVal.setLastTimeLogin(new Date());

        return retVal;
    }
}
