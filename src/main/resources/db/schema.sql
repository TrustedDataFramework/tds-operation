create table if not exists user (
id bigint not null primary key auto_increment,
name varchar(100),
password varchar(200),
role varchar(200),
login_time varchar (255));


create table if not exists node (
id bigint not null primary key auto_increment,
nodeip varchar(200),
nodePort varchar(200),
nodeType varchar(200),
nodeState varchar(200),
nodeVersion varchar(200),
userName varchar(200),
password varchar(200),
leveldbPath varchar(200));


-- create table if not exists mail (
-- id bigint not null primary key auto_increment,
-- sender varchar(200),
-- receiver varchar(200),
-- password varchar(200));


