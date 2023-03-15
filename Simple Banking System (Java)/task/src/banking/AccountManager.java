package banking;

import java.sql.*;
import java.util.concurrent.ThreadLocalRandom;

public class AccountManager {
    //Make this a singleton class
    private static AccountManager INSTANCE = new AccountManager();

    private AccountManager() {
    }

    private String dbFileName;

    public void setDbFileName(String dbFileName) {
        this.dbFileName = dbFileName;
    }

    public synchronized static AccountManager getInstance() {
        return INSTANCE;
    }

    public String generateCardNumber() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        //A 16-digit random card number     
        String cardNumber = String.format("400000%09d", random.nextInt(1000000000));
        //The last digit is the checksum
        int checksum = calculateChecksum(cardNumber);
        //The card number is valid
        return cardNumber + checksum;
    }

    private int calculateChecksum(String cardNumber) {
        int sum = 0;
        for (int i = 0; i < cardNumber.length(); i++) {
            int digit = cardNumber.charAt(i) - '0';
            if (i % 2 == 0) {
                digit *= 2;
            }
            if (digit > 9) {
                digit -= 9;
            }
            sum += digit;
        }
        return (10 - sum % 10) % 10;
    }

    public String generatePIN() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return String.format("%04d", random.nextInt(10000));
    }

    public synchronized void addAccount(Account account) {
        //Insert an entry into the card table
        String url = "jdbc:sqlite:" + this.dbFileName;
        String sql = "INSERT INTO card(number, pin, balance) VALUES( ?, ?, ?)";

        try(Connection conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account.getCardNumber());
            pstmt.setString(2, account.getPin());
            pstmt.setInt(3, account.getBalance());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public synchronized Account getAccount(String cardNumber, String pin) {
        //Read an entry from the card table
        String url = "jdbc:sqlite:" + this.dbFileName;
        String sql = "SELECT number, pin, balance FROM card WHERE number = ? AND pin = ?";
        try(Connection conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardNumber);
            pstmt.setString(2, pin);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Account(rs.getString("number"), rs.getString("pin"), rs.getInt("balance"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public synchronized Account getAccount(String cardNumber) {
        //Read an entry from the card table
        String url = "jdbc:sqlite:" + this.dbFileName;
        String sql = "SELECT number, pin, balance FROM card WHERE number = ?";
        try(Connection conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Account(rs.getString("number"), rs.getString("pin"), rs.getInt("balance"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }


    public void createDbFile() {
        //Create a sqlite db file
        String url = "jdbc:sqlite:" + this.dbFileName;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        //Create new table card if it doesn't exit yet
        String sql = "CREATE TABLE IF NOT EXISTS card (\n"
                + "	id INTEGER PRIMARY KEY,\n"
                + "	number TEXT,\n"
                + "	pin TEXT,\n"
                + " balance INTEGER DEFAULT 0\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateAccount(Account account) {
        //Update an entry in the card table
        String url = "jdbc:sqlite:" + this.dbFileName;
        String sql = "UPDATE card SET balance = ? WHERE number = ?";

        try(Connection conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, account.getBalance());
            pstmt.setString(2, account.getCardNumber());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean isValidCardNumber(String transferCardNumber) {
        //check if the card us a valid card per the Luhn's algorithm
        if (transferCardNumber.length() != 16) {
            return false;
        }
        int checksum = calculateChecksum(transferCardNumber.substring(0, 15));
        return checksum == transferCardNumber.charAt(15) - '0';
    }

    public void deleteAccount(Account account) {
        //Delete an entry from the card table
        String url = "jdbc:sqlite:" + this.dbFileName;
        String sql = "DELETE FROM card WHERE number = ?";

        try(Connection conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account.getCardNumber());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

