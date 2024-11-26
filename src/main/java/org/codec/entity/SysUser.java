package org.codec.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("sys_user")
public class SysUser {
    
    @TableId
    private Long userId;
    
    @TableField("tenant_id")
    private String tenantId;
    
    @TableField("dept_id")
    private Long deptId;
    
    @TableField("user_name")
    private String userName;
    
    @TableField("nick_name")
    private String nickName;
    
    @TableField("user_type")
    private String userType;
    
    @TableField("email")
    private String email;
    
    @TableField("phonenumber")
    private String phonenumber;
    
    @TableField("sex")
    private String sex;
    
    @TableField("avatar")
    private Long avatar;
    
    @TableField("password")
    private String password;
    
    @TableField("status")
    private String status;
    
    @TableField("del_flag")
    private String delFlag;
    
    @TableField("login_ip")
    private String loginIp;
    
    @TableField("login_date")
    private Date loginDate;
    
    @TableField("create_dept")
    private Long createDept;
    
    @TableField("create_by")
    private Long createBy;
    
    @TableField("create_time")
    private Date createTime;
    
    @TableField("update_by")
    private Long updateBy;
    
    @TableField("update_time")
    private Date updateTime;
    
    @TableField("remark")
    private String remark;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Long getAvatar() {
        return avatar;
    }

    public void setAvatar(Long avatar) {
        this.avatar = avatar;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public Date getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    public Long getCreateDept() {
        return createDept;
    }

    public void setCreateDept(Long createDept) {
        this.createDept = createDept;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}