package com.batch.batchCleaner;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BatchService {

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired	
	JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public DataSource dataSource;

	@Autowired
	StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	JobExplorer jobExplorer;
	
	@Autowired
	SimpleJobOperator jobOperator;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	private static final String SQL_DELETE_BATCH_STEP_EXECUTION_CONTEXT = "DELETE FROM BATCH_STEP_EXECUTION_CONTEXT WHERE STEP_EXECUTION_ID IN (SELECT STEP_EXECUTION_ID FROM BATCH_STEP_EXECUTION WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID FROM  BATCH_JOB_EXECUTION where JOB_EXECUTION_ID = ?))";
	private static final String SQL_DELETE_BATCH_STEP_EXECUTION = "DELETE FROM BATCH_STEP_EXECUTION WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION where JOB_EXECUTION_ID = ?)";
	private static final String SQL_DELETE_BATCH_JOB_EXECUTION_CONTEXT = "DELETE FROM BATCH_JOB_EXECUTION_CONTEXT WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID FROM  BATCH_JOB_EXECUTION where JOB_EXECUTION_ID = ?)";
	private static final String SQL_DELETE_BATCH_JOB_EXECUTION_PARAMS = "DELETE FROM BATCH_JOB_EXECUTION_PARAMS WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION where JOB_EXECUTION_ID = ?)";
	private static final String SQL_DELETE_BATCH_JOB_EXECUTION = "DELETE FROM BATCH_JOB_EXECUTION where JOB_EXECUTION_ID = ?";
	private static final String SQL_DELETE_BATCH_JOB_INSTANCE = "DELETE FROM BATCH_JOB_INSTANCE WHERE JOB_INSTANCE_ID NOT IN (SELECT JOB_INSTANCE_ID FROM BATCH_JOB_EXECUTION)";

	@Bean
	@StepScope
	public ItemReader<Product> itemReader(@Value("#{jobParameters['product']}") String productName) {
		return new JdbcCursorItemReaderBuilder<Product>().dataSource(dataSource).name("jdbcCursorItemReader")
				.sql("select brand, name from Batch_Product where name=" + productName).rowMapper(new ProductRowMapper())
				.build();
	}

	@Bean
	public Step productChunkStep() throws Exception {
		return this.stepBuilderFactory.get("productChunkStep").<Product, Product>chunk(1)
				.reader(itemReader(null))
				.writer(new ItemWriter<Product>() {
					@Override
					public void write(List<? extends Product> products) throws Exception {
						products.forEach(System.out::println);
					}
				}).build();
	}

	@Bean
	public Job job() throws Exception {
		return this.jobBuilderFactory.get("job")
				.start(productChunkStep())
				.build();
	}

	@Scheduled(fixedRate = 3600000)
	public void cleanJob() throws NoSuchJobException {
		int count = jobExplorer.getJobInstanceCount("job");
		List<JobInstance> jobInstances = jobExplorer.getJobInstances("job", 0, count);
		for (JobInstance jobInstance : jobInstances) {
			List<JobExecution> jobExplorers = jobExplorer.getJobExecutions(jobInstance);

			for (JobExecution jobExecution : jobExplorers) {
					String productName = jobExecution.getJobParameters().getString("product");
					boolean productExists = productRepository.existsById(productName);
					if(!productExists)
					{
						Long jobExecutionId = jobExecution.getId();
						deleteBatchData(jobExecutionId);
					}
			}
		}

	}
	
	public void deleteBatchData(Long jobExecutionId) {
		jdbcTemplate.update(SQL_DELETE_BATCH_STEP_EXECUTION_CONTEXT, jobExecutionId);
		jdbcTemplate.update(SQL_DELETE_BATCH_STEP_EXECUTION, jobExecutionId);
		jdbcTemplate.update(SQL_DELETE_BATCH_JOB_EXECUTION_CONTEXT, jobExecutionId);
		jdbcTemplate.update(SQL_DELETE_BATCH_JOB_EXECUTION_PARAMS, jobExecutionId);
		jdbcTemplate.update(SQL_DELETE_BATCH_JOB_EXECUTION, jobExecutionId);
		jdbcTemplate.update(SQL_DELETE_BATCH_JOB_INSTANCE, jobExecutionId);
	}
	
	public void saveProduct(String name, String brand) {
		Product product = new Product();
		product.setBrand(brand);
		product.setName(name);
		productRepository.save(product);
	}

	public void deleteProduct(String name) {
		productRepository.deleteById(name);
	}
}
