create table if not exists customer
(
    "00" integer,
    "01" text,
    "02" text,
    "03" integer,
    "04" text,
    "05" numeric,
    "06" text,
    "07" text
);

alter table customer
    owner to myusername;

create table if not exists lineitem
(
    "00" integer,
    "01" integer,
    "02" integer,
    "03" integer,
    "04" integer,
    "05" numeric,
    "06" numeric,
    "07" numeric,
    "08" text,
    "09" text,
    "10" text,
    "11" text,
    "12" text,
    "13" text,
    "14" text,
    "15" text
);

alter table lineitem
    owner to myusername;

create table if not exists nation
(
    "00" integer,
    "01" text,
    "02" integer,
    "03" text
);

alter table nation
    owner to myusername;

create table if not exists orders
(
    "00" integer,
    "01" integer,
    "02" text,
    "03" numeric,
    "04" text,
    "05" text,
    "06" text,
    "07" integer,
    "08" text
);

alter table orders
    owner to myusername;

create table if not exists part
(
    "00" integer,
    "01" text,
    "02" text,
    "03" text,
    "04" text,
    "05" integer,
    "06" text,
    "07" numeric,
    "08" text
);

alter table part
    owner to myusername;

create table if not exists partsupp
(
    "00" integer,
    "01" integer,
    "02" integer,
    "03" numeric,
    "04" text
);

alter table partsupp
    owner to myusername;

create table if not exists region
(
    "00" integer,
    "01" text,
    "02" text
);

alter table region
    owner to myusername;

create table if not exists supplier
(
    "00" integer,
    "01" text,
    "02" text,
    "03" integer,
    "04" text,
    "05" numeric,
    "06" text
);

alter table supplier
    owner to myusername;

create table if not exists images
(
    id        serial
        constraint images_pk
            primary key,
    bangs     integer,
    blackhair integer,
    gender    integer
);

alter table images
    owner to myusername;
