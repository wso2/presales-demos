# Introduction
Sometimes reading data from a table can cause OOM issues when that table contains a huge amount of data. WSO2 EI supports reading data in a streaming manner in cases where users need to just read the data and send it back to the client without doing any processing. In a scenario where we need to do some transformations and operate on the data set at the mediation layer, this streaming mode cannot be used. In this kind of a scenario, we can achieve this requirement with a simple customization to the product.

![](https://github.com/chanakaudaya/class-mediator-database-paging/blob/master/WSO2%20EI%20Process%20large%20data%20set.png)

As depicted in the above diagram, in this use case, the requirement is to read data from a database and push that information (row by row) to 2 backend systems in the same order which is stored in the database. We need to use certain set of capabilities available in the WSO2 EI to implement this use case.

- Paging
- Cloning
- Message Store
- Message Processor
- Class mediator

# Implementation
This simple class mediator can be used in a database paging requirement related use case. Along with the class mediator, there are synapse configurations which can be used to implement an end to end use case where data is pulled from a database and delivered to a backend service in a guaranteed and in-order manner.

- This implementation starts with a proxy service (this can be an API or Sequence) which calls a data service to get the count of all the rows available at the database table.

- Then this count is set to a synapse property.

- After that, there is a class mediator (written in Java) which reads the "count" value and take a parameter called "limit" which will decide the number of records read at once from the database when processing. This parameter allows users to get rid of any OOM errors and provide more control to the processing step. Within this class mediator, it will loop through the chunks of data of "limit" size and call a sequence which is stored in registry under "conf" section.

- Within the sequence, it will take the "startValue" and "limitValue" parameters which were set in the class mediator and call the dataservice with these parameters to read exactly that segment of the table (from "startValue" a chunk of "limitValue") and then iterate through this data set, clone and store them one by one in 2 different message stores. These message stores are backed by message queues created in WSO2 EI Broker profile which is running with offset 3. You should run this within your local machine to test this implementation. This process will be repeated from the class mediator until the table data is fully read.

- The message store will store these messages in the same order which were present in the database table. Then a message processor is configured to consume these messages in the same order (FIFO) and send to an endpoint.

- This endpoint will process the message and give a response. If there is a failure in the endpoint, the message processor will retry 4 times and then deactivate without processing further messages. This will prevent messages from losing.

- Once the process is completed, the messages are delivered to the endpoint in the same order which were stored in the database table and you can verify that by looking at the log file. 

- Make sure to save the "dblookupsequence" in the registry under "conf" path since that is the path referred by the class mediator. 

# Sample database structure

+-----------+--------+---------------+-------------+---------+--------------+

| AccountID | Branch | AccountNumber | AccountType | Balance | ModifiedDate |

+-----------+--------+---------------+-------------+---------+--------------+

|         1 | AOB    | A00012        | CURRENT     |  231221 | 2014-12-02   |

|         2 | AOB    | A00012        | CURRENT     |  231221 | 2014-12-02   |

|         3 | AOB    | A00012        | CURRENT     |  231221 | 2014-12-02   |

|         4 | AOB    | A00012        | CURRENT     |  231221 | 2014-12-02   |

|         5 | AOB    | A00012        | CURRENT     |  231221 | 2014-12-02   |

|         6 | AOB    | A00012        | CURRENT     |  231221 | 2014-12-02   |

|         7 | AOB    | A00012        | CURRENT     |  231221 | 2014-12-02   |

|         8 | AOB    | A00012        | CURRENT     |  231221 | 2014-12-02   |

|         9 | AOB    | A00012        | CURRENT     |  231221 | 2014-12-02   |

|        10 | AOB    | A00012        | CURRENT     |  231221 | 2014-12-02   |

+-----------+--------+---------------+-------------+---------+--------------+

