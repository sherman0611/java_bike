create table Addresses
(
    addressID int         not null
        primary key,
    postcode  varchar(10) not null,
    houseNum  int         not null,
    roadName  varchar(30) null,
    cityName  varchar(30) null
);

create table Brands
(
    brandID   int auto_increment
        primary key,
    brandName varchar(15) null
);

create table Components
(
    brandID  int  not null,
    serial   int  not null,
    price    int  null,
    name     text null,
    quantity int  null,
    primary key (brandID, serial),
    constraint Components_ibfk_1
        foreign key (brandID) references Brands (brandID)
);

create table Customers
(
    customerID int auto_increment
        primary key,
    addressID  int         not null,
    forename   varchar(30) null,
    surname    varchar(30) null,
    constraint CustomerAddressFK
        foreign key (addressID) references Addresses (addressID)
);

create table FrameSets
(
    brandID int        not null,
    serial  int        not null,
    shocks  tinyint(1) null,
    size    int        null,
    gears   int        null,
    primary key (brandID, serial),
    constraint frameset_component_fk
        foreign key (brandID, serial) references Components (brandID, serial)
            on delete cascade
);

create table Handlebars
(
    brandID        int                                  not null,
    serial         int default 0                        not null,
    handlebarStyle enum ('STRAIGHT', 'HIGH', 'DROPPED') null,
    primary key (brandID, serial),
    constraint Handlebar_Components_null_null_fk
        foreign key (brandID, serial) references Components (brandID, serial)
            on delete cascade
);

create table Staff
(
    staffID        int auto_increment
        primary key,
    username       varchar(32) not null,
    hashedPassword text        not null,
    constraint unique_username
        unique (username)
);

create table Orders
(
    orderNumber int                                        not null
        primary key,
    customerID  int                                        not null,
    date        datetime                                   null,
    status      enum ('PENDING', 'CONFIRMED', 'FULFILLED') null,
    staff       varchar(32)                                null,
    bikeName    varchar(32)                                null,
    bikeSerial  bigint                                     null,
    bikeBrand   text                                       null,
    constraint Orders_Staff_username_fk
        foreign key (staff) references Staff (username),
    constraint Orders_ibfk_1
        foreign key (customerID) references Customers (customerID)
);

create table OrderComponents
(
    orderNumber     int not null,
    componentBrand  int not null,
    componentSerial int not null,
    primary key (orderNumber, componentBrand, componentSerial),
    constraint OrderComponents_ibfk_1
        foreign key (orderNumber) references Orders (orderNumber),
    constraint OrderComponents_ibfk_2
        foreign key (componentBrand, componentSerial) references Components (brandID, serial)
);

create index componentBrand
    on OrderComponents (componentBrand, componentSerial);

create index customerID
    on Orders (customerID);

create table Wheels
(
    brandID    int                                 not null,
    serial     int                                 not null,
    diameter   int                                 null,
    wheelStyle enum ('ROAD', 'MOUNTAIN', 'HYBRID') null,
    brakes     enum ('RIM', 'DISK')                null,
    primary key (brandID, serial),
    constraint Wheels_ibfk_1
        foreign key (brandID, serial) references Components (brandID, serial)
            on delete cascade
);


