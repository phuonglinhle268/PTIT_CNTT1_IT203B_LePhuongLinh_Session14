package dao;

import entity.OrderItem;
import entity.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private final ProductDAO productDAO = new ProductDAO();

    public boolean placeOrder(int userId, List<OrderItem> items) {
        Connection conn = null;
        try {
            conn = DatabaseConnectionManager.getInstance().getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            double totalAmount = 0.0;
            List<Product> lockedProducts = new ArrayList<>();

            // Kiểm tra tồn kho
            for (OrderItem item : items) {
                Product p = productDAO.getProductWithLock(conn, item.getProductId());
                if (p == null || p.getStockQuantity() < item.getQuantity()) {
                    throw new SQLException("Hết hàng: " + (p != null ? p.getName() : "Sản phẩm"));
                }
                lockedProducts.add(p);
                totalAmount += p.getPrice() * item.getQuantity();
            }

            // Trừ stock
            for (int i = 0; i < items.size(); i++) {
                productDAO.updateStock(conn, items.get(i).getProductId(), items.get(i).getQuantity());
            }

            // Tạo Order
            String orderSql = "INSERT INTO Orders (user_id, total_amount) VALUES (?, ?)";
            int orderId;
            try (PreparedStatement psOrder = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, userId);
                psOrder.setDouble(2, totalAmount);
                psOrder.executeUpdate();

                ResultSet rs = psOrder.getGeneratedKeys();
                orderId = rs.next() ? rs.getInt(1) : 0;
            }

            String detailSql = "INSERT INTO Order_Details (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psDetail = conn.prepareStatement(detailSql)) {
                for (int i = 0; i < items.size(); i++) {
                    OrderItem item = items.get(i);
                    Product p = lockedProducts.get(i);
                    psDetail.setInt(1, orderId);
                    psDetail.setInt(2, item.getProductId());
                    psDetail.setInt(3, item.getQuantity());
                    psDetail.setDouble(4, p.getPrice());
                    psDetail.addBatch();
                }
                psDetail.executeBatch();
            }

            conn.commit();
            System.out.println("Đặt hàng thành công");
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("Đặt hàng thất bại");
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public void top5Buyers() {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall("{call SP_GetTopBuyers()}");
             ResultSet rs = cs.executeQuery()) {

            System.out.println("\nTOP 5 người mua nhiều nhất");
            while (rs.next()) {
                System.out.printf("%s | Tổng tiền: %.2f | Số đơn: %d%n",
                        rs.getString("username"), rs.getDouble("total_spent"), rs.getInt("num_orders"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void categoryRevenue() {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall("{call SP_GetCategoryRevenue()}");
             ResultSet rs = cs.executeQuery()) {

            System.out.println("\nDoanh thu theo danh mục");
            while (rs.next()) {
                System.out.printf("Danh mục %-15s : %.2f VND%n",
                        rs.getString("category"), rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
