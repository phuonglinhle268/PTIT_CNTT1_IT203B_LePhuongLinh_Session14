import dao.*;
import entity.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        DBInitializer.initialize();

        UserDAO userDAO = new UserDAO();
        ProductDAO productDAO = new ProductDAO();
        OrderDAO orderDAO = new OrderDAO();

        userDAO.createUser("user1", "user1@gmail.com");

        Product flashProduct = new Product("iPhone", "Smartphone", 25000000, 10);
        productDAO.createProduct(flashProduct);

        int testProductId = 1;

        List<Thread> threads = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);   // Sửa lỗi successCount

        for (int i = 1; i <= 50; i++) {
            Thread t = new Thread(() -> {
                boolean success = orderDAO.placeOrder(1, List.of(new OrderItem(testProductId, 1)));
                if (success) {
                    successCount.incrementAndGet();
                }
            });
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\nThống kê:");
        System.out.println("   Số đơn thành công : " + successCount.get());

        orderDAO.top5Buyers();
        orderDAO.categoryRevenue();

    }
}