package com.m4g.dtos;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Created by MaxG on 17-Apr-15.
 */
@XmlRootElement(name = "userInfo")
public class UserInfo {
    private Date lastTimeLogin;

    public UserInfo() {
    }

    @XmlElement
    public Date getLastTimeLogin() {
        return lastTimeLogin;
    }

    public void setLastTimeLogin(Date lastTimeLogin) {
        this.lastTimeLogin = lastTimeLogin;
    }
}
