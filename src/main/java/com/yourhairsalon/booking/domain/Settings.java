package com.yourhairsalon.booking.domain;

import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.yourhairsalon.booking.reference.ClientDisplay;

public class Settings {
	
	public Settings(){
		
	}
	
	public Settings(ShopSettings settings){
		this.setVersion(settings.getVersion());
		this.setId(settings.getId());
		this.setReceiveConfirmations(settings.isReceiveConfirmations());
		this.setEmail_subject(settings.getEmail_subject());
		this.setEmail_message(settings.getEmail_message());
		this.setEmail_signature(settings.getEmail_signature());
		this.setUsername(settings.getUsername());
		this.setUser_password(settings.getUser_password());
		this.setStore_phone(settings.getStore_phone());
		this.setDefault_appt_length(settings.getDefault_appt_length());
		this.setClientdisplay(settings.getClientdisplay());
		this.setImage_file_logo(settings.getImage_file_logo());
		this.setShop(settings.getShop());
	}
	
	private Integer version;
	
	private Long id;
	
    private boolean receiveConfirmations;

    private String email_address;
    
    private String email_subject;

    private String email_message;

    private String email_signature;

    private String username;

    private String user_password;

    private String store_phone;

    private String default_appt_length;

    private ClientDisplay clientdisplay;

    private String image_file_logo;

    private Shop shop;

	public void setReceiveConfirmations(boolean receiveConfirmations) {
		this.receiveConfirmations = receiveConfirmations;
	}

	public boolean isReceiveConfirmations() {
		return receiveConfirmations;
	}

	public void setEmail_subject(String email_subject) {
		this.email_subject = email_subject;
	}

	public String getEmail_subject() {
		return email_subject;
	}

	public void setEmail_message(String email_message) {
		this.email_message = email_message;
	}

	public String getEmail_message() {
		return email_message;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setEmail_signature(String email_signature) {
		this.email_signature = email_signature;
	}

	public String getEmail_signature() {
		return email_signature;
	}

	public void setUser_password(String user_password) {
		this.user_password = user_password;
	}

	public String getUser_password() {
		return user_password;
	}

	public void setStore_phone(String store_phone) {
		this.store_phone = store_phone;
	}

	public String getStore_phone() {
		return store_phone;
	}

	public void setDefault_appt_length(String default_appt_length) {
		this.default_appt_length = default_appt_length;
	}

	public String getDefault_appt_length() {
		return default_appt_length;
	}

	public void setImage_file_logo(String image_file_logo) {
		this.image_file_logo = image_file_logo;
	}

	public String getImage_file_logo() {
		return image_file_logo;
	}

	public void setClientdisplay(ClientDisplay clientdisplay) {
		this.clientdisplay = clientdisplay;
	}

	public ClientDisplay getClientdisplay() {
		if(clientdisplay == null){
			clientdisplay = ClientDisplay.LASTNAME;
		}
		return clientdisplay;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}

	public Shop getShop() {
		return shop;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Integer getVersion() {
		return version;
	}

	public void setEmail_address(String email_address) {
		this.email_address = email_address;
	}

	public String getEmail_address() {
		return email_address;
	}

}
