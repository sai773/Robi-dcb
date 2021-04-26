package com.juno.util;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SecureImageResponse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@SerializedName("image1")
	@Expose
    private String image1;
	
    @SerializedName("image2")
    @Expose
    private String image2;
    
    @SerializedName("statusCode")
    @Expose
    private String statusCode;
    
    @SerializedName("optxn")
    @Expose
    private String optxn;
    
    @SerializedName("pimage")
	@Expose
	private String pimage;
    
    @SerializedName("result")
   	@Expose
   	private String result;
}
