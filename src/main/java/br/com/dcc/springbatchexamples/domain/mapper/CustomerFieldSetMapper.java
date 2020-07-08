package br.com.dcc.springbatchexamples.domain.mapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import br.com.dcc.springbatchexamples.domain.Customer;

public class CustomerFieldSetMapper implements FieldSetMapper<Customer> {

	@Override
	public Customer mapFieldSet(FieldSet fieldSet) throws BindException {
		return Customer.builder()
				.email(fieldSet.readString("email"))
				.firstName(fieldSet.readString("firstName"))
				.lastName(fieldSet.readString("lastName"))
				.build();
	}

}
