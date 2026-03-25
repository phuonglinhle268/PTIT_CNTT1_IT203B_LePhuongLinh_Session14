package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBInitializer {
    public static void initialize() {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("""
                    create table Users (
                        user_id int auto_increment primary key,
                        username VARCHAR(50) unique not null,
                        email VARCHAR(100) unique not null ,
                        created_at timestamp default current_timestamp
                    )""");

            stmt.executeUpdate("""
                    create table Products (
                        product_id int auto_increment primary key,
                        name varchar(100) not null ,
                        category varchar(50),
                        price decimal(10,2) not null ,
                        stock_quantity int not null check(stock_quantity >= 0),
                        created_at timestamp default  current_timestamp
                    )""");

            stmt.executeUpdate("""
                    create table Orders (
                        order_id int auto_increment primary key,
                        user_id int not null,
                        order_date timestamp default  current_timestamp,
                        total_amount decimal(10,2),
                        foreign key (user_id) references Users(user_id) on delete cascade 
                    )""");

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS Order_Details (
                        order_detail_id int auto_increment primary key,
                        order_id int not null,
                        product_id int not null,
                        quantity int not null check (quantity > 0),
                        price_at_purchase decimal(10,2) not null,
                        foreign key (order_id) references Orders(order_id) on delete cascade,
                        foreign key (product_id) references Products(product_id)
                    )""");

            System.out.println("Database tables đã được khởi tạo");
        } catch (SQLException e) {
            System.err.println("Lỗi khởi tạo");
            e.printStackTrace();
        }
    }
}
