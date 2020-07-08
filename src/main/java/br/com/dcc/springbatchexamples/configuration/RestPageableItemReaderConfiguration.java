package br.com.dcc.springbatchexamples.configuration;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.dcc.springbatchexamples.domain.Customer;
import br.com.dcc.springbatchexamples.reader.RestPageableItemReader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RestPageableItemReaderConfiguration {

	@Value("subscriptions.url")
	private String subcriptionsUrl;

	@Bean
	public RestPageableItemReader restPageableItemReader() throws Exception {
		String url = "http://localhost:8080/customers/page";
		int size = 20;
		String sort = "id,ASC";
		return new RestPageableItemReader(url, size, sort);
	}


	@Bean
	public ItemWriter<Customer> restPageableItemWriter() {
		return new ItemWriter<Customer>() {
			@Override
			public void write(List<? extends Customer> items) throws Exception {
				for (Customer item : items) {
					log.info(">> {}", item);
				}
			}
		};
	}

	@Bean
	public Step restPageableItemReaderStep1(StepBuilderFactory stepBuilderFactory) throws Exception {
		return stepBuilderFactory.get("RestPageableItemReaderStep1")
				.<Customer, Customer>chunk(20)
				.reader(restPageableItemReader())
				.writer(restPageableItemWriter())
				.stream(restPageableItemReader())
				.build();
	}

	@Bean
	public Job restPageableItemReaderJob(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) throws Exception {
		return jobBuilderFactory.get("RestPageableItemReaderJob")
				.start(restPageableItemReaderStep1(stepBuilderFactory))
				.build();

	}

}
