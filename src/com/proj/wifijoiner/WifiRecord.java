/**
 * 
 */
package com.proj.wifijoiner;

/**
 * @author Jogi
 * This class holds the data necessary to add/modify a wifi network
 */
public class WifiRecord {

	private String ssid;
	private String secret;
	private String security;
	/**
	 * @return the ssid
	 */
	public String getSsid() {
		return ssid;
	}
	/**
	 * @param ssid the ssid to set
	 */
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	/**
	 * @return the secret
	 */
	public String getSecret() {
		return secret;
	}
	/**
	 * @param secret the secret to set
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}
	/**
	 * @return the security
	 */
	public String getSecurity() {
		return security;
	}
	/**
	 * @param security the security to set
	 */
	public void setSecurity(String security) {
		this.security = security;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WifiRecord [ssid=" + ssid + ", secret=" + secret
				+ ", security=" + security + "]";
	}
}
