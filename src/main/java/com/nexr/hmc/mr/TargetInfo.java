package com.nexr.hmc.mr;

public class TargetInfo {
	String host;
	String path;
	String userId;
	String pwd;
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
    @Override
    public String toString() {
        return "TargetInfo [host=" + host + ", path=" + path + ", userId=" + userId + ", pwd=" + pwd + "]";
    }
	
	
}
