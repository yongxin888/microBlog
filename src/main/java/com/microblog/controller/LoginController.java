package com.microblog.controller;

import com.google.code.kaptcha.Producer;
import com.microblog.entity.LoginTicket;
import com.microblog.entity.User;
import com.microblog.service.UserService;
import com.microblog.util.CommunityConstant;
import com.microblog.util.CommunityUtil;
import com.microblog.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 登录、登出、注册
 * @DATE: 2023/4/20 17:54
 */
@Controller
public class LoginController implements CommunityConstant {

    //在IDE控制台打印日志
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer producer;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 进入注册界面
     * @return
     */
    @GetMapping("/register")
    public String getRegisterPage() {
        return "site/register";
    }

    /**
     * 进入登录界面
     * @return
     */
    @GetMapping("/login")
    public String getLoginPage() {
        return "site/login";
    }

    /**
     * 进入重置密码界面
     */
    @GetMapping("/resetPwd")
    public String getResetPwdPage() {
        return "site/reset-pwd";
    }

    /**
     * 用户注册
     * @param model 模板
     * @param user 用户数据
     * @return
     */
    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.insertUser(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功!");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        }else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "site/register";
        }
    }

    /**
     * 生成验证码，并存入 Redis
     * @param response 返回图片
     */
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response) {
        //生成验证码
        String text = producer.createText();    //生成随机字符
        System.out.println("验证码：" + text);
        BufferedImage image = producer.createImage(text);

        //验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);   //60秒后过期
        response.addCookie(cookie);

        //将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败", e.getMessage());
        }
    }



    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param rememberMe 是否记住我（点击记住我后，凭证的有效期延长）
     * @param model
     * @param kaptchaOwner 从 cookie 中取出的 kaptchaOwner
     * @param response 响应数据
     * @return
     */
    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam("code") String code,
                        @RequestParam(value = "rememberMe", required = false) boolean rememberMe,
                        @CookieValue("kaptchaOwner") String kaptchaOwner,
                        Model model, HttpServletResponse response) {
        //检查验证码
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner); //从Cookie中拿到验证码标识
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey); //用标识去redis中拿验证码数据
        }

        //判断验证码是否有误 equalsIgnoreCase不区分大小写比较是否相等
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码错误");
            return "site/login";
        }

        //凭证过期时间 点击记住我的话凭证时间为30天
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;

        //验证用户名和密码
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        //判断该key在map中是否存在
        if (map.containsKey("ticket")) {
            //账号和密码都正确，则服务端会生成ticket，浏览器通过 cookie 存储 ticket
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setMaxAge(expiredSeconds); //cookie有效期
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/login";
        }
    }

    /**
     * 用户登出
     * @param ticket 设置凭证状态为无效
     * @return
     */
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket) {
        // 修改（先删除再插入）对应用户在 redis 中的凭证状态
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        //修改凭证状态
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
