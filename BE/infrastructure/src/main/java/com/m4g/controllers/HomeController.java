package com.m4g.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by MaxG on 18-Apr-15.
 */
@Controller
public class HomeController {

    @RequestMapping(value = "/home")
    public String displayHome(Model model) {
        return "/index.jsp";
    }

    @RequestMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public User jsonTest(HttpServletResponse response, @RequestParam String user, @RequestParam String pass) {
        if(user.equals("max") && pass.equals("qwe")){
            return new User("1","Max");
        }
        else {
            response.setStatus( HttpServletResponse.SC_BAD_REQUEST  );
            return null;
        }
    }

    @RequestMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Product> productsTest() {
        List retVal = new LinkedList<Product>();
        retVal.add(new Product("http://4.bp.blogspot.com/-iL2-WB3wg9g/TcRZbR_oQEI/AAAAAAAAGIc/ll1DF20CbBQ/s320/Nature%2BWallpapers%2B%252811%2529.jpg","one"));
        retVal.add(new Product("http://www.hdiphonewallpapers.us/phone-wallpapers/phone/12963B64236440-2J38.jpg","two"));
        retVal.add(new Product("https://33.media.tumblr.com/avatar_5c66595177d4_128.png","three"));
        retVal.add(new Product("https://31.media.tumblr.com/avatar_574ac89d2a85_128.png","four"));
        retVal.add(new Product("http://www2.hiren.info/download/mobile-phone/wallpapers/phone-wallpaper-b8e.jpg","five"));

        return retVal;
    }

    class User{
        String id;
        String name;

        public User(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    class Product{
        String url;
        String name;

        public Product(String url, String name) {
            this.url = url;
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}