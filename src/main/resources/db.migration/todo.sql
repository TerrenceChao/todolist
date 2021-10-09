
drop table if exists todo_task_1;
create table todo_task_1 (
                             tid bigint(41) primary key,
                             title varchar(20) not null,
                             content varchar(200) not null,
                             attachments json null,
                             week_of_year int(2) not null,
                             created_at datetime not null,
                             deleted_at datetime null,
                             done datetime null
);

drop table if exists todo_task_2;
create table todo_task_2 (
                             tid bigint(41) primary key,
                             title varchar(20) not null,
                             content varchar(200) not null,
                             attachments json null,
                             week_of_year int(2) not null,
                             created_at datetime not null,
                             deleted_at datetime null,
                             done datetime null
);







select * from todo_list_1;
select * from todo_list_2;

drop table if exists todo_list_1;
create table todo_list_1 (
                             lid bigint(41) primary key,
                             todo_tasks json not null,
                             first_created_at datetime not null,
                             first_month int(2) not null,
                             first_week_of_year int(2) not null,
                             last_created_at datetime null,

                             next_month int(2) not null,
                             next_week_of_year int(2) not null,
                             next_lid bigint(41) null
);

drop table if exists todo_list_2;
create table todo_list_2 (
                             lid bigint(41) primary key,
                             todo_tasks json not null,
                             first_created_at datetime not null,
                             first_month int(2) not null,
                             first_week_of_year int(2) not null,
                             last_created_at datetime null,

                             next_month int(2) not null,
                             next_week_of_year int(2) not null,
                             next_lid bigint(41) null
);
