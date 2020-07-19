# SpringBatchCleanup
Demo Spring Batch Program which cleans old batch jobs triggered using parameter, where parameter used is a value from table column which no longer exists

In this program, we have used mysql database (product) and no prefix used in the creation of spring batch related tables. Table BATCH_PRODUCT is created here to display the job execution and to help cleanup operation. BATCH_PRODUCT has two columns name and brand, where name is primary key.

Demo program has REST Controllers:
1. GET /product/{name}/brand/{brand} -- Add Product table
2. DELETE /product/{name} -- Deletes Product with name
3. GET /cleanJobs -- Starts cleanup process (Also, added Scheduler with auto-trigger clean job at fixedRate at certain intervals)
4. GET /startJob/{productName} -- Starts Spring batch job with parameter name product and value equal to {productName} given in the URL path.

To check this program we can simply add products and then trigger their spring jobs. After deletion of any product, we can trigger URL to clean up the spring batch tables or it will auto-trigger at certain intervals. Now, we can verify that all the tables data, corresponding to the product parameter, is deleted from table. Below are the table whose data is cleared which are created in the spring batch framework.

![TDD](https://github.com/codingeass/SpringBatchCleanup/blob/master/Spring%20Batch%20Tables%20CleanedUp.png)
