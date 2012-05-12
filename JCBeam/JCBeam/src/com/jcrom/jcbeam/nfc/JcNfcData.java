package com.jcrom.jcbeam.nfc;

import java.io.Serializable;

public class JcNfcData implements Serializable{
	public String name;
	public String address;
	public String filePath;
	public boolean beamAvailable;

	public static final String ENABLE = "ENABLE";

	public JcNfcData(String name, String address, String path, boolean isEnable) {

		this.name     = name;
		this.address  = address;
		this.filePath = path;
		this.beamAvailable = isEnable;
	}


	public JcNfcData() {
        // TODO 自動生成されたコンストラクター・スタブ
    }


    public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getFilePath() {
		return filePath;
	}

   public String getAvailable() {

       if(beamAvailable){
           return ENABLE;
       }

       return "DON'T CREATE ZIPFILE";
    }

   public boolean isEnable() {
       return beamAvailable;
    }

	public void setName(String name) {
		this.name = name;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}