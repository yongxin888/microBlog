package com.microblog.service.Impl;

import com.microblog.entity.LoginTicket;
import com.microblog.entity.User;
import com.microblog.mapper.UserMapper;
import com.microblog.service.UserService;
import com.microblog.util.CommunityConstant;
import com.microblog.util.CommunityUtil;
import com.microblog.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION:  用户表
 * @DATE: 2023/4/18 15:10
 */
@Service
public class UserServiceImpl implements UserService, CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据ID查询用户信息
     * @param id 用户ID
     * @return  用户数据
     */
    @Override
    public User selectById(int id) {
        //先查询缓存中是否有用户数据
        String userKey = RedisKeyUtil.getUserKey(id);
        User user = (User) redisTemplate.opsForValue().get(userKey);
        //如果缓存中没有用户数据，则查询数据库
        if (user != null) {
            return user;
        }else {
            User selectUser = userMapper.selectById(id);
            redisTemplate.opsForValue().set(userKey, selectUser, 3600, TimeUnit.SECONDS);
            return selectUser;
        }
    }

    /**
     * 用户信息变更时清除对应缓存数据
     * @param userId 用户id
     */
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    /**
     * 用户注册
     * @param user 用户数据
     * @return Map<String, Object> 返回错误提示消息，如果返回的 map 为空，则说明注册成功
     */
    @Override
    public Map<String, Object> insertUser(User user) {
        Map<String, Object> map = new HashMap<>();

        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        //判断用户名是否为空
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
        }

        //判断密码是否为空
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "密码不能为空");
        }

        //判断邮箱是否为空
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("usernameMsg", "邮箱不能为空");
        }

        //验证账号是否存在
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在");
            return map;
        }

        //验证邮箱是否已注册
        User email = userMapper.selectByEmail(user.getEmail());
        if (email != null) {
            map.put("emailMsg", "该邮箱已注册");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5)); //salt用户密码加密
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));   //加盐加密
        user.setType(0); // 默认普通用户
        user.setStatus(1); // 默认为激活
        user.setActivationCode(CommunityUtil.generateUUID()); // 激活码
        // 随机头像（用户登录后可以自行修改）
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date()); // 注册时间
        userMapper.insertUser(user);
        return map;
    }

    /**
     * 根据用户名查询用户是否存在
     * @param username 用户名
     * @return 用户数据
     */
    @Override
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    /**
     * 用户登录（为用户创建凭证）
     * @param username 用户名
     * @param password 密码
     * @param expiredSeconds 多少秒后凭证过期
     * @return Map<String, Object> 返回错误提示消息以及 ticket(凭证)
     */
    @Override
    public Map<String, Object> login(String username, String password, Integer expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账户不存在");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            // 账号未激活
            map.put("usernameMsg", "该账号未激活");
            return map;
        }

        //验证密码是否正确
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码错误");
            return map;
        }

        //如果用户名密码都正确，则生成该用户登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId()); //用户ID
        loginTicket.setStatus(0); // 设置凭证状态为有效（当用户登出的时候，设置凭证状态为无效）
        loginTicket.setTicket(CommunityUtil.generateUUID()); //生成随机凭证
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000)); //凭证到期时间

        //将登录凭证存入Redis
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * 根据 ticket 查询 LoginTicket 信息
     * @param ticket 用户登录凭证
     * @return
     */
    @Override
    public LoginTicket findLoginTicket(String ticket) {
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    /**
     * 修改用户密码（对新密码加盐加密存入数据库）
     * @param userId 用户id
     * @param newPassword 新密码
     * @return
     */
    @Override
    public int updatePassword(int userId, String newPassword) {
        User user = userMapper.selectById(userId);
        // 重新加盐加密
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        clearCache(userId);
        return userMapper.updatePassword(userId, newPassword);
    }

    /**
     * 获取某个用户的权限
     * @param userId
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.selectById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
