insert into shopmedb.users(email,enabled,first_name,last_name,password,account_non_locked) values
("omariit@gmail.com",1,'khalil','alomari','$2a$10$2N6UdI/RhuoPfhunMdlgEunBTwTTluPsJUguCMNQR0ghBaoVp.RkO',
b'1');
insert into shopmedb.roles(name,description) values("Admin","admin");
insert into shopmedb.roles(name,description) values("Salesperson","sales");
insert into shopmedb.roles(name,description) values("Editor","editor");
insert into shopmedb.roles(name,description) values("Shipper","shipper");
insert into shopmedb.roles(name,description) values("Assistant","assistant");

insert into  shopmedb.users_roles(user_id,role_id) values(1,1);
