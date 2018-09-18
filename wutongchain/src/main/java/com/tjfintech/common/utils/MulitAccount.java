package com.tjfintech.common.utils;

public class MulitAccount {
    private String address;
    private String pubKey1;
    private String pubKey2;
    private String pubKey3;
    private String priKey1;
    private String priKey2;
    private String priKey3;
    private String pwd;

    /**
     * 1/2账号
     * @param address
     * @param pubKey1
     * @param pubKey2
     * @param priKey1
     * @param priKey2
     */
    public MulitAccount(String address, String pubKey1, String pubKey2, String priKey1, String priKey2) {
        this.address = address;
        this.pubKey1 = pubKey1;
        this.pubKey2 = pubKey2;
        this.priKey1 = priKey1;
        this.priKey2 = priKey2;
    }

    /**
     * 2/3账号
     * @param address
     * @param pubKey1
     * @param pubKey2
     * @param pubKey3
     * @param priKey1
     * @param priKey2
     * @param priKey3
     * @param pwd
     */
    public MulitAccount(String address, String pubKey1, String pubKey2, String pubKey3, String priKey1, String priKey2, String priKey3, String pwd) {
        this.address = address;
        this.pubKey1 = pubKey1;
        this.pubKey2 = pubKey2;
        this.pubKey3 = pubKey3;
        this.priKey1 = priKey1;
        this.priKey2 = priKey2;
        this.priKey3 = priKey3;
        this.pwd = pwd;
    }

    public MulitAccount(String address, String pubKey1, String pubKey2, String pubKey3, String priKey1, String priKey2, String priKey3) {
        this.address = address;
        this.pubKey1 = pubKey1;
        this.pubKey2 = pubKey2;
        this.pubKey3 = pubKey3;
        this.priKey1 = priKey1;
        this.priKey2 = priKey2;
        this.priKey3 = priKey3;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPubKey1() {
        return pubKey1;
    }

    public void setPubKey1(String pubKey1) {
        this.pubKey1 = pubKey1;
    }

    public String getPubKey2() {
        return pubKey2;
    }

    public void setPubKey2(String pubKey2) {
        this.pubKey2 = pubKey2;
    }

    public String getPubKey3() {
        return pubKey3;
    }

    public void setPubKey3(String pubKey3) {
        this.pubKey3 = pubKey3;
    }

    public String getPriKey1() {
        return priKey1;
    }

    public void setPriKey1(String priKey1) {
        this.priKey1 = priKey1;
    }

    public String getPriKey2() {
        return priKey2;
    }

    public void setPriKey2(String priKey2) {
        this.priKey2 = priKey2;
    }

    public String getPriKey3() {
        return priKey3;
    }

    public void setPriKey3(String priKey3) {
        this.priKey3 = priKey3;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
