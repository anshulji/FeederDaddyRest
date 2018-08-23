package com.dev.fd.feederdaddyrest.model;
public class User{

    private String username,password,restaurantname,phone,restaurantid,city,restauranttype;

    public User() {
    }

    public User(String username, String password, String restaurantname, String phone, String restaurantid, String city, String restauranttype) {
        this.username = username;
        this.password = password;
        this.restaurantname = restaurantname;
        this.phone = phone;
        this.restaurantid = restaurantid;
        this.city = city;
        this.restauranttype = restauranttype;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUsername() {
        return username;

    }

    public String getRestaurantname() {
        return restaurantname;
    }

    public void setRestaurantname(String restaurantname) {
        this.restaurantname = restaurantname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRestaurantid() {
        return restaurantid;
    }

    public void setRestaurantid(String restaurantid) {
        this.restaurantid = restaurantid;
    }

    public String getRestauranttype() {
        return restauranttype;
    }

    public void setRestauranttype(String restauranttype) {
        this.restauranttype = restauranttype;
    }
}
