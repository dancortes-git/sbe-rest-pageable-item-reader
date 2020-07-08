package br.com.dcc.springbatchexamples.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import br.com.dcc.springbatchexamples.domain.Customer;
import br.com.dcc.springbatchexamples.domain.mapper.CustomerFieldSetMapper;
import br.com.dcc.springbatchexamples.listener.SimpleChunkListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ReadFromFileConfiguration {

	@Bean
	public FlatFileItemReader<Customer> readFromFileReader() {

		FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();
		reader.setLinesToSkip(1);
		reader.setResource(new ClassPathResource("customer.csv"));
		DefaultLineMapper<Customer> customerLineMapper = new DefaultLineMapper<>();
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setNames("email", "firstName", "lastName");
		customerLineMapper.setLineTokenizer(tokenizer);
		customerLineMapper.setFieldSetMapper(new CustomerFieldSetMapper());
		customerLineMapper.afterPropertiesSet();
		reader.setLineMapper(customerLineMapper);

		return reader;
	}

	@Bean
	public ItemWriter<Customer> readFromFileWriter() {
		return items -> {
			for (Customer item : items) {
				log.info("Writing item {}", item.toString());
			}
		};
	}

	@Bean
	public Step readFromFileStep1(StepBuilderFactory stepBuilderFactory) {
		return stepBuilderFactory.get("ReadFromFileStep1")
				.<Customer, Customer>chunk(10)
				.faultTolerant()
				.listener(new SimpleChunkListener())
				.reader(readFromFileReader())
				.writer(readFromFileWriter())
				.build();
	}

	@Bean
	public Job readFromFileJob(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
		return jobBuilderFactory.get("ReadFromFileJob")
				.start(readFromFileStep1(stepBuilderFactory))
				.build();

	}

}
