package cn.skyeye.ibase;

import cn.skyeye.common.net.HttpPoster;
import cn.skyeye.common.net.Https;

public  class Name{
    private String name;

    public Name(String name) {
        this.name = name;
    }

    public Name() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static void main(String[] args) throws Exception {
        HttpPoster post = Https.post("http://localhost:8088/skyeye/", true);
        post.addParam("name", "jechedo");
        String execute = post.execute();
        System.out.println(execute);
    }
}