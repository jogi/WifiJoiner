/**
 * 
 */
package com.proj.wifijoiner.scanner;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.proj.wifijoiner.Caesar;
import com.proj.wifijoiner.WifiRecord;

/**
 * @author Jogi
 * 
 */
public final class ConnectButtonListener implements Button.OnClickListener {
	private final WifiRecord record;
	private final String TAG = "ConnectButtonListener";
	private Context context;
	private WifiManager wfMgr;

	public ConnectButtonListener(Context context, WifiRecord record) {
		this.context = context;
		this.record = record;
	}

	public void onClick(View view) {
		Log.d(TAG, "onClick() Addnetwork()");

		Log.i(TAG, record.toString());
		
		String ssid = record.getSsid();
		String password = record.getSecret();
		String type = record.getSecurity();
		boolean encrypted = record.isEncrypted();
		
		//if the password is encrypted then decrypt it
		if(encrypted) {
			password = new Caesar().decrypt(password);
		}

		WifiConfiguration wfc = new WifiConfiguration();

		wfc.SSID = "\"".concat(ssid).concat("\"");
		wfc.status = WifiConfiguration.Status.DISABLED;
		wfc.priority = 40;


		if(type.equalsIgnoreCase("open")) {
			wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			wfc.allowedAuthAlgorithms.clear();
			wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			
		} else if(type.equalsIgnoreCase("WEP")) {
			wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

//			if (StringUtils.sHex(password)) 
	//			wfc.wepKeys[0] = password;
		//	else 
				wfc.wepKeys[0] = "\"".concat(password).concat("\"");
			wfc.wepTxKeyIndex = 0;
			
		} else if(type.equalsIgnoreCase("WPA/WPA2")) {
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

			wfc.preSharedKey = "\"".concat(password).concat("\"");			
		} else {
			//dont know what to do. Exit?
		}
		
		

		wfMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		WifiConfiguration found = findNetworkInExistingConfig(wfc.SSID);
	    wfMgr.disconnect();
	    if (found == null) {	      
	    } else {	      
	      Log.d(TAG, "Removing network " + found.networkId);
	      wfMgr.removeNetwork(found.networkId);
	      wfMgr.saveConfiguration();
	    }
		int networkId = wfMgr.addNetwork(wfc);
		if (networkId != -1) {
			// success, can call wfMgr.enableNetwork(networkId, true) to
			// connect
			if(isNetworkInRange(wfc.SSID))
				wfMgr.enableNetwork(networkId, true);
			else
				wfMgr.enableNetwork(networkId, false);
			
			new AlertDialog.Builder(context)	        
	        .setTitle("New network has been configured! Do you want to exit?")
	        .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {

	                /* User clicked OK so do some stuff */
	            	System.exit(0);
	            }
	        })
	        .setCancelable(true)
	        .create().show();
		}
	}
	
	/**
	   * If the given ssid name exists in the settings, then change its password to the one given here, and save
	   * @param ssid
	   */
	  private WifiConfiguration findNetworkInExistingConfig(String ssid){
	    List<WifiConfiguration> existingConfigs = wfMgr.getConfiguredNetworks();	    
	    for (WifiConfiguration existingConfig : existingConfigs) {
	      if (existingConfig.SSID.equals(ssid)) {
	        return existingConfig;
	      }
	    }
	    return null;
	  }
	  
	  private boolean isNetworkInRange(String ssid) {
		  List<ScanResult> networks = wfMgr.getScanResults();
		  
		  for(ScanResult result : networks) {
			  if (result.SSID.equals(ssid))
				  return true;
		  }
		  
		  return false;
	  }
}
