package com.batch.batchCleaner;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name="BATCH_PRODUCT")
public class Product {

	@Id
	private String name;
	private String brand;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	
}
