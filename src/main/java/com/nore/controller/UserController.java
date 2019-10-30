package com.nore.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nore.pojo.Menu;
import com.nore.pojo.Role;
import com.nore.pojo.User;
import com.nore.service.RoleService;
import com.nore.service.UserService;
import com.nore.utils.MyStatus;
import com.nore.vo.SysUserVO;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Set;

/**
 * @Author:nore
 */
@Controller
@RequestMapping("/sys")
@CrossOrigin("http://localhost:8082")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;

    @RequestMapping("/login")
    @ResponseBody
    public MyStatus userLogin(@RequestBody SysUserVO sysUserVO){
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token=new UsernamePasswordToken(sysUserVO.getUsername(),sysUserVO.getPassword());
        subject.login(token);
        return new MyStatus(0);
    }

    @RequestMapping("/user/info")
    @ResponseBody
    public MyStatus userInfo(){
        Subject subject = SecurityUtils.getSubject();
        String nowUsername = (String)subject.getPrincipal();
        User user=userService.queryUserByUsername(nowUsername);
        return MyStatus.ok().put("user",user);
    }

    @RequestMapping("/menu/user")
    @ResponseBody
    public MyStatus menuUser(){
        Subject subject = SecurityUtils.getSubject();
        String username = (String)subject.getPrincipal();
        Set<Menu> menus = roleService.queryMenuByUsername(username);
        Set<String> permissions = roleService.queryAllMenuByUsername(username);
        return MyStatus.ok().put("menuList",menus).put("permissions",permissions);
    }

    @RequestMapping("/logout")
    @ResponseBody
    public MyStatus logout(){
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return MyStatus.ok();
    }

    @RequestMapping("/user/list")
    @ResponseBody
    public MyStatus findAllUser(String order,Integer limit,Integer offset){
        PageHelper.orderBy("user_id "+order);
        PageHelper.startPage(offset/limit+1,limit);
        List<User> allUser = userService.findAllUser();
        PageInfo<User> pageInfo=new PageInfo<>(allUser);
        return MyStatus.ok().put("total",pageInfo.getTotal()).put("rows",pageInfo.getList());
    }

    @RequestMapping("/roles")
    @ResponseBody
    public MyStatus findRoles(){
        List<Role> roles=roleService.findRoles();
        return MyStatus.ok().put("roles",roles);
    }
    


}
