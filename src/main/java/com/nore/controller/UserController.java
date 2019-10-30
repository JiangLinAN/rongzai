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
import org.springframework.web.bind.annotation.*;

import java.util.Date;
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
    public MyStatus userLogin(@RequestBody SysUserVO sysUserVO) {
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(sysUserVO.getUsername(), sysUserVO.getPassword());
        subject.login(token);
        return new MyStatus(0);
    }

    @RequestMapping("/user/info")
    @ResponseBody
    public MyStatus userInfo() {
        Subject subject = SecurityUtils.getSubject();
        String nowUsername = (String) subject.getPrincipal();
        User user = userService.queryUserByUsername(nowUsername);
        return MyStatus.ok().put("user", user);
    }

    @RequestMapping("/menu/user")
    @ResponseBody
    public MyStatus menuUser() {
        Subject subject = SecurityUtils.getSubject();
        String username = (String) subject.getPrincipal();
        Set<Menu> menus = roleService.queryMenuByUsername(username);
        Set<String> permissions = roleService.queryAllMenuByUsername(username);
        return MyStatus.ok().put("menuList", menus).put("permissions", permissions);
    }

    @RequestMapping("/logout")
    @ResponseBody
    public MyStatus logout() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return MyStatus.ok();
    }

    @RequestMapping("/user/list")
    @ResponseBody
    public MyStatus findAllUser(String order, Integer limit, Integer offset) {
        PageHelper.orderBy("user_id " + order);
        PageHelper.startPage(offset / limit + 1, limit);
        List<User> allUser = userService.findAllUser();
        PageInfo<User> pageInfo = new PageInfo<>(allUser);
        return MyStatus.ok().put("total", pageInfo.getTotal()).put("rows", pageInfo.getList());
    }

    @RequestMapping("/roles")
    @ResponseBody
    public MyStatus findRoles() {
        List<Role> roles = roleService.findRoles();
        return MyStatus.ok().put("roles", roles);
    }

    @RequestMapping("user/save")
    @ResponseBody
    public MyStatus userSave(@RequestBody SysUserVO userVO) {
        Subject subject = SecurityUtils.getSubject();
        Integer createUserId = userService.queryUserByUsername((String) subject.getPrincipal()).getUserId();
        userVO.setCreateUserId(createUserId);

        Integer count = userService.userSave(userVO);
        System.out.println("用户表修改了:" + count);

        Integer size = 0;
        for (Integer role : userVO.getRoles()) {
            size = roleService.userSaveRole(userVO.getUserId(), role);
        }
        System.out.println("用户角色联系表修改了:" + size);
        return MyStatus.ok();
    }

    @RequestMapping("/user/info/{userId}")
    @ResponseBody
    public MyStatus userUpdate(@PathVariable("userId") Integer userId) {
        SysUserVO sysUserVO = userService.queryUserByUserId(userId);
        List<Integer> roles = roleService.queryRoleIdByUserId(userId);
        sysUserVO.setRoles(roles);
        return MyStatus.ok().put("user", sysUserVO);
    }

    @RequestMapping("/user/update")
    @ResponseBody
    public MyStatus userUp(@RequestBody SysUserVO sysUserVO) {
        System.out.println(sysUserVO);
        Integer count=userService.updateUser(sysUserVO);
        User user = userService.queryUserByUsername(sysUserVO.getUsername());
        System.out.println("更新数据库:"+count);

        Integer size= roleService.deleRoleByUserId(sysUserVO.getUserId());
        System.out.println("数据库中删除了 "+size+" 条角色信息");
        List<Integer> roles = sysUserVO.getRoles();
        Integer rolecount=0;
        for (Integer role : roles) {
            System.out.println("添加角色"+role);
            roleService.userSaveRole(user.getUserId(),role);
            rolecount++;
        }
        System.out.println("数据库中添加了"+rolecount+" 角色信息");
        return MyStatus.ok();
    }

    @RequestMapping("/user/del/{userId}")
    @ResponseBody
    public MyStatus deleUser(@PathVariable("userId") Integer userId){
        Integer count =userService.deleUser(userId);
        System.out.println("删除用户数据"+count+" 条");
        Integer integer = roleService.deleRoleByUserId(userId);
        System.out.println("删除角色信息数量"+integer);
        return MyStatus.ok();
    }
}
