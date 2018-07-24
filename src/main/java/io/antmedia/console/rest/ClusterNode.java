package io.antmedia.console.rest;

public class ClusterNode {
	
	private String id;
	private String ip;
	private String status;
	
	public ClusterNode() {
		// TODO Auto-generated constructor stub
	}
	
	public ClusterNode(String id, String ip) {
		super();
		this.id = id;
		this.ip = ip;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
