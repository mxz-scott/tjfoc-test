package com.tjfintech.common.utils;



public class Account {
    private String address;
    private String priKey;
    private String pubKey;
    private String pwd;

    public Account(String address, String priKey, String pubKey, String pwd) {
        this.address = address;
        this.priKey = priKey;
        this.pubKey = pubKey;
        this.pwd = pwd;
    }

    public Account(String address, String priKey, String pubKey) {
        this.address = address;
        this.priKey = priKey;
        this.pubKey = pubKey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
