package com.m4g.controllers;

import com.m4g.ApiTestContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Created by MaxG on 17-Apr-15.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApiTestContext.class})
@WebAppConfiguration
public class UserInfoControllerTest {

    private MockMvc mockMvc;

    @Resource
    private WebApplicationContext applicationContext;

    @Before
    public void setup(){
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    public void testGetUserInfo() throws Exception {
        mockMvc.perform(get("/userInfo/kjh22g34g54g56gh7"));
    }
}
