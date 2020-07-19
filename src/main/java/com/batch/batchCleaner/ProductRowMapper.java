package com.batch.batchCleaner;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class ProductRowMapper implements RowMapper<Product> {

	@Override
	public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
		Product product = new Product();
		product.setBrand(rs.getString("brand"));
		product.setName(rs.getString("name"));
		return product;
	}

}
