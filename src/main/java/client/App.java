package client;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.sql.ResultSet;
import org.apache.ignite.sql.SqlRow;

public class App {
    public static void main(String[] args) {
        try (IgniteClient client = IgniteClient.builder().addresses("127.0.0.1:10800").build()) {
            System.out.println("Ignite baglantisi basarili! 🚀");

            // 1. Tabloyu oluştur
            String createTableSql = "CREATE TABLE IF NOT EXISTS Subscriber (" +
                    "customerId VARCHAR PRIMARY KEY, " +
                    "dataUsage DOUBLE, " +
                    "smsUsage INT, " +
                    "callUsage INT)";
            client.sql().execute(null, createTableSql);

            // 2. Her çalıştırmada temiz bir başlangıç için tabloyu temizle (Ödev zorunluluğu)
            client.sql().execute(null, "DELETE FROM Subscriber");
            System.out.println("Tablo temizlendi.");

            // 3. Başlangıç değerleri 0 olan 5 dummy Subscriber nesnesi oluştur (Ödev zorunluluğu)
            List<Subscriber> subscribers = Arrays.asList(
                new Subscriber("CUST-1", 0.0, 0, 0),
                new Subscriber("CUST-2", 0.0, 0, 0),
                new Subscriber("CUST-3", 0.0, 0, 0),
                new Subscriber("CUST-4", 0.0, 0, 0),
                new Subscriber("CUST-5", 0.0, 0, 0)
            );

            // 4. Nesneleri veritabanına ekle
            for (Subscriber sub : subscribers) {
                String insertSql = String.format(
                    "INSERT INTO Subscriber (customerId, dataUsage, smsUsage, callUsage) VALUES ('%s', %s, %d, %d)",
                    sub.getCustomerId(), sub.getDataUsage(), sub.getSmsUsage(), sub.getCallUsage()
                );
                client.sql().execute(null, insertSql);
            }
            System.out.println("5 yeni abone eklendi.");

            // 5. Kullanımları rastgele değerlerle simüle et ve veritabanını güncelle
            Random random = new Random();
            ResultSet<SqlRow> resultSet = client.sql().execute(null, "SELECT customerId FROM Subscriber");
            
            while (resultSet.hasNext()) {
                SqlRow row = resultSet.next();
                String id = row.stringValue("customerId");
                
                double extraData = Math.round((random.nextDouble() * 5.0) * 100.0) / 100.0;
                int extraSms = random.nextInt(50);
                int extraCall = random.nextInt(100);

                String updateSql = String.format(
                    "UPDATE Subscriber SET dataUsage = dataUsage + %s, smsUsage = smsUsage + %d, callUsage = callUsage + %d WHERE customerId = '%s'",
                    extraData, extraSms, extraCall, id
                );
                client.sql().execute(null, updateSql);
            }
            System.out.println("Kullanim verileri güncellendi.");

            // 6. Son durumu ekrana yazdır
            System.out.println("\n--- GUNCEL ABONE DURUMLARI ---");
            ResultSet<SqlRow> finalResults = client.sql().execute(null, "SELECT * FROM Subscriber");
            while (finalResults.hasNext()) {
                SqlRow row = finalResults.next();
                System.out.printf("Abone: %s | Data: %.2f GB | SMS: %d | Arama: %d dk%n",
                        row.stringValue("customerId"),
                        row.doubleValue("dataUsage"),
                        row.intValue("smsUsage"),
                        row.intValue("callUsage"));
            }
            
        } catch (Exception e) {
            System.err.println("Bir hata olustu: " + e.getMessage());
        }
    }
}