package com.batch.batchCleaner;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BatchController {
	
	@Autowired
	BatchService batchService;
	
	@Autowired
	JobLauncher jobLauncher;
	
	@Autowired
	Job job;

	@GetMapping("/product/{name}/brand/{brand}")
	public void addProduct(@PathVariable String name, @PathVariable String brand) {
		batchService.saveProduct(name, brand);
	}
	
	@DeleteMapping("/product/{name}")
	public void addProduct(@PathVariable String name) {
		batchService.deleteProduct(name);
	}
	
	@GetMapping("/cleanJobs")
	public void cleanBatchJobs() throws NoSuchJobException {
		batchService.cleanJob();
	}
	
	@GetMapping("/startJob/{productName}")
	public void startJob(@PathVariable String productName) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		JobParameters jobParameters = new JobParametersBuilder().addString("product", productName).toJobParameters();
        jobLauncher.run(job, jobParameters);
	}
	
}
