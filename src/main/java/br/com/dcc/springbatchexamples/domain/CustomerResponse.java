package br.com.dcc.springbatchexamples.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

	private List<Customer> content;

	private Pageable pageable;

	private int totalPages;

	private int totalElements;

	private int size;

	private int numberOfElements;

	@Data
	private class Pageable {

		private int offset;
		private int pageNumber;
		private int pageSize;


	}
}
