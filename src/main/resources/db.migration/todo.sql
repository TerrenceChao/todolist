
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;




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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;




## https://dev.mysql.com/doc/refman/8.0/en/create-index.html
drop table if exists attachment_1;
create table attachment_1 (
                              aid char(32) primary key,
                              created_at bigint(13) not null
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

drop table if exists attachment_2;
create table attachment_2 (
                              aid char(32) primary key,
                              created_at bigint(13) not null
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;