
-- DB schema for ROQI 
--

create SEQUENCE hibernate_sequence START 1;

create table subject (
    subject_id bigint primary key,
	label varchar default ' ',
--	description varchar default ' ', 
	uri varchar not null
);

create table registry_resource (
	resource_id bigint primary key,
	description varchar default ' ',
	identifier varchar not null, 
	rights varchar default ' ',
	short_name varchar default ' ',
	title varchar default ' ',
	footprint varchar default ' ',
	waveband varchar default 'unknown', 
	validation_by varchar default 'unknown', 
	validation_value varchar default 'unknown'
);

create table interface (
    interface_id bigint primary key,
	query_type varchar default ' ',
	result_type varchar default ' ', 
	role varchar default ' ', 
	xsitype varchar default ' ', 
	use varchar default 'unknown', 
	url varchar default ' ' 
);

create table capability (
    capability_id bigint primary key,
	interface_id_fk bigint,
	foreign key (interface_id_fk) references interface (interface_id),
	standard_id varchar default ' ',
	xsitype varchar default ' '
);

create table ucd (
    ucd_id bigint primary key,-- default nextval('hibernate_sequence')::bigint,
	name varchar default ' ',
	description varchar default ' ', 
	uri varchar not null
);

-- associations

create table subject_resource_assoc (
	resource_id bigint,
	foreign key (resource_id) references registry_resource (resource_id),
	subject_id bigint, 
	foreign key (subject_id) references subject (subject_id), 
	unique (subject_id, resource_id)
);

create table resource_capability_assoc (
	resource_id bigint,
	foreign key (resource_id) references registry_resource (resource_id),
	capability_id bigint unique,
	foreign key (capability_id) references capability (capability_id)
);

create table resource_ucd_assoc (
	resource_id bigint,
	foreign key (resource_id) references registry_resource (resource_id),
	ucd_id bigint, 
	foreign key (ucd_id) references ucd (ucd_id), 
	unique (ucd_id, resource_id)
);
