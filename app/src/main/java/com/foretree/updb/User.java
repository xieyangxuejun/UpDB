package com.foretree.updb;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.Date;

/**
 * Created by silen on 13/04/2018.
 */

@Entity
public class User {
    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private String name;

    private int age = 1;

    private String job;
    private boolean isBoy;

    private int int1;
    private String string1;
    private Date date;


    @Generated(hash = 586692638)
    public User() {
    }


    @Generated(hash = 1576044054)
    public User(Long id, @NotNull String name, int age, String job, boolean isBoy,
            int int1, String string1, Date date) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.job = job;
        this.isBoy = isBoy;
        this.int1 = int1;
        this.string1 = string1;
        this.date = date;
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", job='" + job + '\'' +
                ", isBoy=" + isBoy +
                ", int1=" + int1 +
                ", string1='" + string1 + '\'' +
                ", date=" + date +
                '}';
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public boolean isBoy() {
        return isBoy;
    }

    public void setBoy(boolean boy) {
        isBoy = boy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
//
//    public int getAge() {
//        return age;
//    }
//
//    public void setAge(int age) {
//        this.age = age;
//    }
//
//    public boolean getIsBoy() {
//        return this.isBoy;
//    }
//
//    public void setIsBoy(boolean isBoy) {
//        this.isBoy = isBoy;
//    }


    public int getAge() {
        return this.age;
    }


    public void setAge(int age) {
        this.age = age;
    }


    public boolean getIsBoy() {
        return this.isBoy;
    }


    public void setIsBoy(boolean isBoy) {
        this.isBoy = isBoy;
    }


    public int getInt1() {
        return this.int1;
    }


    public void setInt1(int int1) {
        this.int1 = int1;
    }


    public String getString1() {
        return this.string1;
    }


    public void setString1(String string1) {
        this.string1 = string1;
    }


    public Date getDate() {
        return this.date;
    }


    public void setDate(Date date) {
        this.date = date;
    }
}
