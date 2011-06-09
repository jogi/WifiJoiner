/**
 * 
 */
package com.proj.wifijoiner;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.proj.wifijoiner.qr.Contents;
import com.proj.wifijoiner.qr.Intents;

/**
 * @author Jogi
 *
 */
public class CreateActivity extends Activity implements TextWatcher {

	private ArrayList<String> wifiStrings = new ArrayList<String>();
	private List<WifiConfiguration> wifiConfigs = new ArrayList<WifiConfiguration>();
 	private WifiManager wifi;
 	
 	private EditText textSSID;
 	private EditText textSecret;
 	private Spinner typeSpinner;
 	private Spinner existingSpinner;
 	private Button buttonCreate;
 	private CheckBox checkShowPassword;
	
	void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }	
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.create);
		
		//Ads
		// Look up the AdView as a resource and load a request.
	    AdView adView = (AdView)this.findViewById(R.id.adView);
	    adView.loadAd(new AdRequest());
		
		textSSID = (EditText) findViewById(R.id.ssid_text_view);
		textSSID.addTextChangedListener(this);
		
		textSecret = (EditText) findViewById(R.id.secret_text_view);
		textSecret.addTextChangedListener(this);
		
		buttonCreate = (Button) findViewById(R.id.create_button);
		
		typeSpinner = (Spinner) findViewById(R.id.type_spinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this, R.array.security, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setSelected(true);
        typeSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {                        
                        validate();
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        showToast("Security: unselected");
                    }
                });
        
        
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiConfigs = wifi.getConfiguredNetworks();
        
        wifiStrings.add("None");
        
        for(WifiConfiguration config : wifiConfigs) {
        	if (!config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP))
        		wifiStrings.add(removeDoubleQuotes(config.SSID));
        }
        
        existingSpinner = (Spinner) findViewById(R.id.existing_spinner);
        ArrayAdapter<String> existingAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, wifiStrings);
        existingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        existingSpinner.setAdapter(existingAdapter);
        existingSpinner.setSelected(true);
        existingSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {                        
                        selectNetwork(position);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        showToast("Network: unselected");
                    }
                });
        
        checkShowPassword = (CheckBox) findViewById(R.id.secret_check_box);
        checkShowPassword.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Perform action on clicks, depending on whether it's now checked
                if (((CheckBox) v).isChecked()) {
                    textSecret.setTransformationMethod(null);
                } else {
                    textSecret.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });
        
        buttonCreate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				WifiRecord record = new WifiRecord();
				record.setSsid(textSSID.getText().toString());
				record.setSecurity(typeSpinner.getSelectedItem().toString());
				record.setSecret(textSecret.getText().toString());
				
				Intent intent = new Intent(Intents.Encode.ACTION);
			    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
			    intent.putExtra(Intents.Encode.DATA, new Gson().toJson(record));
			    intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());			    
			    
			    startActivity(intent);
			}
        	
        });
        
        validate();
		
	}
	
	public void selectNetwork(int position) {
		//if none selected then clear up all text
		if(wifiStrings.get(position).equals("None")) {
			textSSID.setText("");
			textSecret.setText("");			
		} else {
			WifiConfiguration selectedConfig = wifiConfigs.get(position-1); //first one is None
						

			//WPA/WPA2
			if (selectedConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK))
			{
				textSSID.setText(removeDoubleQuotes(selectedConfig.SSID));
			    typeSpinner.setSelection(1, true);			    
			    validate();
			}
			//Open
			else if (selectedConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE) && 
					selectedConfig.allowedAuthAlgorithms.isEmpty())
			{
				textSSID.setText(removeDoubleQuotes(selectedConfig.SSID));
			    typeSpinner.setSelection(0, true);			    
			    validate();
			}
			//WEP
			else if (selectedConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE) && 
					!selectedConfig.allowedAuthAlgorithms.isEmpty())
			{
				textSSID.setText(removeDoubleQuotes(selectedConfig.SSID));
			    typeSpinner.setSelection(2, true);
			    validate();
			}
			else
			{
			    // not one of the above.... weird...
				validate();
			}

		}
	}
	
	static String removeDoubleQuotes(String string) {
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }
	
	private void validate() {
        // TODO: make sure this is complete.
		String ssid = textSSID.getText().toString();
		String secret = textSecret.getText().toString();
		int security = typeSpinner.getSelectedItemPosition();

		if ((ssid != null && ssid.length() == 0)
				|| ((security == 2 && secret.length() == 0) || 
						(security == 1 && secret.length() < 8))) {
			buttonCreate.setEnabled(false);
		} else {
			buttonCreate.setEnabled(true);
        }
    }

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub		
		validate();
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		
		
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {		
		
	}

}
