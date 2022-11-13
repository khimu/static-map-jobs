Setup

Setup java8
Create directory /opt/yellowpages-data/
copy googleapi.properties, log4j2.xml, and app-all.jar from target directory
create mysql database and tables
insert data accordingly
copy all the executable bash scripts to the directory
when using aws ec2, create the VPC, create the ec2, create the RDS for the EC2, Create documentDB, and then update googleapi.properties
To scp, add your id_rsa.pub to .ssh/authorized_keys in ec2 instance
make sure to change user from root to ec2-root

scp api-scrapper-executable.jar ec2-user@54.210.48.70:/opt/yellowpages-data/app-all.jar


# Allows for creating a platform independent script to run jar
gradle wrapper

Usage
  java -jar -Xmx2048m -Xms1024m -Dlog4j.configuration="file:/opt/log4j2.xml" target/api-scrapper-executable.jar cleanStoreJob spring/batch/jobs/clean-store-data.xml /opt/geocode/google.properties

Note 
	After creating topics table run command below

  	mysql -uroot -p -h business.cpmenyilehdo.us-east-1.rds.amazonaws.com  --local-infile=1 -e "use business; LOAD DATA LOCAL INFILE '/opt/yellowpages-data/topics.csv' INTO TABLE topic #FIELDS TERMINATED BY '\t' ENCLOSED BY '' LINES STARTING BY '' TERMINATED BY '\n' (code);"




-Dlogging.config='/path/to/log4j2.xml'


CREATE DATABASE business;


CREATE TABLE IF NOT EXISTS job_state (
  id BIGINT(11) NOT NULL AUTO_INCREMENT,
  job_name VARCHAR(45) DEFAULT NULL,
  last_processed_key BIGINT(11) DEFAULT NULL,
  quota BIGINT(11) DEFAULT NULL,
  already_exist int(20) DEFAULT NULL,
  success int(20) DEFAULT NULL,
  failed int(20) DEFAULT NULL,
  last_processed_date DATETIME DEFAULT NULL,
  created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB;



		
CREATE TABLE IF NOT EXISTS business_data (
  id BIGINT(11) NOT NULL AUTO_INCREMENT,
  name VARCHAR(200) DEFAULT NULL,
  city VARCHAR(100) DEFAULT NULL,
  state VARCHAR(100) DEFAULT NULL,
  address_line_1 VARCHAR(100) DEFAULT NULL,
  address_line_2 VARCHAR(100) DEFAULT NULL,
  zipcode VARCHAR(20) DEFAULT NULL,
  country_code VARCHAR(100) DEFAULT NULL,
  website VARCHAR(200) DEFAULT NULL,
  phone VARCHAR(50) DEFAULT NULL,
  email VARCHAR(200) DEFAULT NULL,
  latitude VARCHAR(100) DEFAULT NULL,
  longitude VARCHAR(100) DEFAULT NULL,
  category VARCHAR(200) DEFAULT NULL,
  full_address varchar(200) DEFAULT NULL,
  key_words text DEFAULT NULL,
  public_store_key text DEFAULT NULL,
  created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT unique_idx1 UNIQUE (name, address_line_1, city, state, zipcode),  
  PRIMARY KEY (id)
) ENGINE=InnoDB;



CREATE TABLE IF NOT EXISTS yellowpages_data (
  id BIGINT(11) NOT NULL AUTO_INCREMENT,
  name VARCHAR(200) DEFAULT NULL,
  city VARCHAR(100) DEFAULT NULL,
  state VARCHAR(100) DEFAULT NULL,
  address_line_1 VARCHAR(100) DEFAULT NULL,
  address_line_2 VARCHAR(100) DEFAULT NULL,
  zipcode varchar(20) DEFAULT NULL,
  country_code VARCHAR(100) DEFAULT NULL,
  website VARCHAR(200) DEFAULT NULL,
  phone VARCHAR(50) DEFAULT NULL,
  email VARCHAR(200) DEFAULT NULL,
  latitude VARCHAR(100) DEFAULT NULL,
  longitude VARCHAR(100) DEFAULT NULL,
  category VARCHAR(200) DEFAULT NULL,
  full_address varchar(200) DEFAULT NULL,  
  key_words text DEFAULT NULL,
  public_store_key text DEFAULT NULL,
  created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT unique_idx1 UNIQUE (name, address_line_1,city, state, zipcode),  
  PRIMARY KEY (id)
) ENGINE=InnoDB



CREATE TABLE IF NOT EXISTS yelp_data (
  id BIGINT(11) NOT NULL AUTO_INCREMENT,
  name VARCHAR(200) DEFAULT NULL,
  city VARCHAR(100) DEFAULT NULL,
  state VARCHAR(100) DEFAULT NULL,
  address_line_1 VARCHAR(100) DEFAULT NULL,
  address_line_2 VARCHAR(100) DEFAULT NULL,
  zipcode varchar(20) DEFAULT NULL,
  country_code VARCHAR(100) DEFAULT NULL,
  website VARCHAR(200) DEFAULT NULL,
  phone VARCHAR(50) DEFAULT NULL,
  email VARCHAR(200) DEFAULT NULL,
  latitude VARCHAR(100) DEFAULT NULL,
  longitude VARCHAR(100) DEFAULT NULL,
  category VARCHAR(200) DEFAULT NULL,
  full_address varchar(200) DEFAULT NULL,  
  key_words text DEFAULT NULL,
  public_store_key text DEFAULT NULL,
  created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT unique_idx1 UNIQUE (name, address_line_1),
  CONSTRAINT unique_idx2 UNIQUE (name, address_line_1,city, state, zipcode),
  PRIMARY KEY (id)
) ENGINE=InnoDB;



CREATE TABLE IF NOT EXISTS yelp_links (
  id BIGINT(11) NOT NULL AUTO_INCREMENT,
  link VARCHAR(200) DEFAULT NULL,
  city VARCHAR(100) DEFAULT NULL,
  state VARCHAR(100) DEFAULT NULL,
  pages INT(100) DEFAULT NULL,
  category VARCHAR(200) DEFAULT NULL,
  created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB;



CREATE TABLE IF NOT EXISTS yellowpages_links (
  id BIGINT(11) NOT NULL AUTO_INCREMENT,
  link VARCHAR(200) DEFAULT NULL,
  city VARCHAR(100) DEFAULT NULL,
  state VARCHAR(100) DEFAULT NULL,
  pages INT(100) DEFAULT NULL,
  category VARCHAR(200) DEFAULT NULL,
  created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS topic (
  id BIGINT(11) NOT NULL AUTO_INCREMENT,
  code VARCHAR(150) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;


-- must meet google daily api quota
INSERT INTO `business`.`job_state`
(`job_name`,`last_processed_key`,`quota`,`already_exist`,`success`,`failed`,`last_processed_date`)
VALUES('staticMapJob',0,25000,0,0,1,NOW() - INTERVAL 1 DAY);

-- 2 million records to process
INSERT INTO `business`.`job_state`
(`job_name`,`last_processed_key`,`quota`,`already_exist`,`success`,`failed`,`last_processed_date`)
VALUES('storeDataUpdateJob',0,2000000,0,0,0,now());

-- 100 categories per job run; Currently has no categories in the topic table to run this job;  Use the link job instead
INSERT INTO `business`.`job_state`
(`job_name`,`last_processed_key`,`quota`,`already_exist`,`success`,`failed`,`last_processed_date`)
VALUES('yellowpagesDataJob',0,100,0,0,0,now());

-- 100 categories per job run; Currently has no categories in the topic table to run this job
INSERT INTO `business`.`job_state`
(`job_name`,`last_processed_key`,`quota`,`already_exist`,`success`,`failed`,`last_processed_date`)
VALUES('yelpDataJob',0,100,0,0,0,now());

-- generates a sitemap for google console to index
INSERT INTO `business`.`job_state`
(`job_name`,`last_processed_key`,`quota`,`already_exist`,`success`,`failed`,`last_processed_date`)
VALUES('crawlerSiteMapJob',0,100,0,0,0,now());


INSERT INTO `business`.`job_state`
(`job_name`,`last_processed_key`,`quota`,`already_exist`,`success`,`failed`,`last_processed_date`)
VALUES('userIdUpdateJob',0,100,0,0,0,now());

-- must meet google daily api quota
INSERT INTO `business`.`job_state`
(`job_name`,`last_processed_key`,`quota`,`already_exist`,`success`,`failed`,`last_processed_date`)
VALUES('cleanStoreJob',0,25000,0,0,0,now());

-- same as cleanStoreJob but can be used for a different table
INSERT INTO `business`.`job_state`
(`job_name`,`last_processed_key`,`quota`,`already_exist`,`success`,`failed`,`last_processed_date`)
VALUES('cleanScrapperJob',0,25000,0,0,0,now());



INSERT IGNORE
  INTO business_data 
SELECT *
  FROM yelp_data
     ;
     
     
     
INSERT IGNORE
  INTO business_data 
SELECT *
  FROM yellowpages_data
     ;



mongo --shell
use business_scrapper;
show collections;
db.bname.find({});
db.bname.find({}).count();
show dbs;


db.bname.createIndex( { store_id: 1 } )
