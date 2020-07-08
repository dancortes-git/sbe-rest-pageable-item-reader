package br.com.dcc.springbatchexamples.reader;

import java.net.URI;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import br.com.dcc.springbatchexamples.domain.Customer;
import br.com.dcc.springbatchexamples.domain.CustomerResponse;
import lombok.Data;

@Data
public class RestPageableItemReader implements ItemStreamReader<Customer> {

	private List<Customer> items;
	private int curIndex = -1;
	private int size = -1;
	private int page = -1;
	private int totalPages = -1;
	private int totalElements = -1;
	private int numberOfReadElements = -1;
	private String sort;
	private boolean restart = false;
	private URI uri;
	private String url;

	public RestPageableItemReader(String url) throws Exception {
		this(url, 0, null, 0);
	}

	public RestPageableItemReader(String url, int size) throws Exception {
		this(url, size, null, 0);
	}

	public RestPageableItemReader(String url, int size, String sort) throws Exception {
		this(url, size, sort, 0);
	}

	public RestPageableItemReader(String url, int size, String sort, int page) throws Exception {

		this.curIndex = 0;
		this.totalPages = 0;
		this.totalElements = 0;
		this.numberOfReadElements = 0;
		URIBuilder uriBuilder = new URIBuilder(url);
		uriBuilder.removeQuery();
		// Setting Size
		if (size > 0) {
			this.size = size;
			uriBuilder.addParameter("size", String.valueOf(this.size));
		} else {
			this.size = 0;
		}
		// Setting sort
		if (StringUtils.hasText(sort)) {
			this.sort = sort;
			uriBuilder.addParameter("sort", this.sort);
		} else {
			this.sort = null;
		}
		// Setting Page
		if (page >= 0) {
			this.page = page;
		} else {
			this.page = 0;
		}
		uriBuilder.addParameter("page", String.valueOf(this.page));
		this.uri = uriBuilder.build();

	}

	@Override
	public Customer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		Customer item = null;

		if (restart && null == this.items) {
			this.items = fetchCustomerData(this.uri);
		}

		if (this.curIndex < this.items.size()) {
			item = this.items.get(this.curIndex);
			this.curIndex++;
			this.numberOfReadElements++;
		} else {
			if (this.numberOfReadElements < this.totalElements) {
				this.uri = updatePageFromUri(this.uri);
				this.items = fetchCustomerData(this.uri);
				this.curIndex=0;
				item = this.items.get(this.curIndex);
				this.curIndex++;
				this.numberOfReadElements++;
			}
		}

		// Just for test propose
		if (item != null && item.getId() == 72 && !restart) {
			throw new RuntimeException("The Answer to the ultimate question of life, the universe, and everything...");
		}

		return item;
	}

	/*
	 * Open method is called only once at the beginning of execution
	 */
	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {

		if (executionContext.containsKey("curIndex")) {
			// In case of restart... (get the parameters from saved state of the last execution)
			this.url = executionContext.getString("url");
			try {
				this.uri = new URI(this.url);
			} catch (Exception e) {
				this.uri = null;
			}
			this.curIndex = executionContext.getInt("curIndex");
			this.page = executionContext.getInt("page");
			this.totalElements = executionContext.getInt("totalElements");
			this.totalPages = executionContext.getInt("totalPages");
			this.size = executionContext.getInt("size");
			this.numberOfReadElements = executionContext.getInt("numberOfReadElements");
			this.restart = true;
		} else {
			// First call...
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<CustomerResponse> response = restTemplate.getForEntity(this.uri, CustomerResponse.class);
			this.totalElements = response.getBody().getTotalElements();
			this.totalPages = response.getBody().getTotalPages();
			this.items = response.getBody().getContent();
			this.size = response.getBody().getSize();
			this.curIndex = 0;
			executionContext.put("url", this.uri.toString());
			executionContext.put("curIndex", this.curIndex);
			executionContext.put("page", this.page);
			executionContext.put("totalElements", this.totalElements);
			executionContext.put("totalPages", this.totalPages);
			executionContext.put("size", this.size);
		}

	}

	/*
	 * Update method is called every chunk
	 */
	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		executionContext.put("url", this.uri.toString());
		executionContext.put("curIndex", this.curIndex);
		executionContext.put("page", this.page);
		executionContext.put("totalElements", this.totalElements);
		executionContext.put("totalPages", this.totalPages);
		executionContext.put("size", this.size);
		executionContext.put("numberOfReadElements", this.numberOfReadElements);
	}

	@Override
	public void close() throws ItemStreamException {
		// TODO Auto-generated method stub

	}

	private URI updatePageFromUri(URI uri) throws Exception {
		URIBuilder uriBuilder = new URIBuilder(uri);
		List<NameValuePair> params =  uriBuilder.getQueryParams();
		uriBuilder.removeQuery();
		for (NameValuePair param : params) {
			if ("page".equalsIgnoreCase(param.getName())) {
				this.page = Integer.parseInt(param.getValue()) + 1;
				uriBuilder.addParameter(param.getName(), String.valueOf(this.page));
			} else {
				uriBuilder.addParameter(param.getName(), param.getValue());
			}
		}
		return uriBuilder.build();
	}

	private List<Customer> fetchCustomerData(URI uri) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<CustomerResponse> response = restTemplate.getForEntity(uri, CustomerResponse.class);
		return response.getBody().getContent();
	}

}
