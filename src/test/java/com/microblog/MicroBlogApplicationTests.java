package com.microblog;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest()
public class MicroBlogApplicationTests {

    @Test
    public void contextLoads() {
        //System.out.println(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        String kaptcha = "abcd";
        String code = "Aacd";
        boolean blank = StringUtils.isBlank(kaptcha);
        System.out.println(blank);
        System.out.println(!kaptcha.equalsIgnoreCase(code));
        if (blank || !kaptcha.equalsIgnoreCase(code)) {
            System.out.println("错误");
        }
    }

    @Test
    public void test1() {

    }

}
