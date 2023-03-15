package banking;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //Read the database file name from the command line arguments.
        if(args[0].equals("-fileName")) {
            AccountManager.getInstance().setDbFileName(args[1]);
            //Crete a new database file if it doesn't exist.
            AccountManager.getInstance().createDbFile();
        }
        int option = 0;
        do {
            printMenu();
            Scanner scanner = new Scanner(System.in);
            option = scanner.nextInt();
            switch (option) {
                case 1 -> createAccount();
                case 2 -> logIntoAccount(scanner);
                case 0 -> doExit();
                default -> System.out.println("Incorrect option! Try again.");
            }
        } while (option != 0);
    }

    private static void doExit() {
        System.out.println("Bye!");
        System.exit(0);
    }

    private static void createAccount() {
        AccountManager accountManager = AccountManager.getInstance();
        String cardNumber = accountManager.generateCardNumber();
        String pin = accountManager.generatePIN();

        Account account = new Account(cardNumber, pin, 0);
        accountManager.addAccount(account);

        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(cardNumber);
        System.out.println("Your card PIN:");
        System.out.println(pin);
    }

    private static void logIntoAccount(Scanner scanner) {
        System.out.println("Enter your card number:");
        String cardNumber = scanner.nextLine();
        if(cardNumber.isBlank()) {
            cardNumber = scanner.nextLine();
        }

        System.out.println("Enter your PIN:");
        String pin = scanner.nextLine();
        if(pin.isBlank()) {
            pin = scanner.nextLine();
        }

        AccountManager accountManager = AccountManager.getInstance();
        Account account = accountManager.getAccount(cardNumber, pin);
        if (account != null) {
            System.out.println("You have successfully logged in!");
            int option = 0;
            do {
                printAccountMenu();
                option = scanner.nextInt();

                switch (option) {
                    case 1 -> System.out.println("Balance: " + account.getBalance());
                    case 2 -> addIncome(scanner, accountManager, account);
                    case 3 -> doTransfer(scanner, accountManager, account);
                    case 4 -> closeAccount(accountManager, account);
                    case 5 -> System.out.println("You have successfully logged out!");
                    case 0 -> doExit();
                    default -> System.out.println("Incorrect option! Try again.");
                }
            } while(option != 5 && option != 0);

        } else {
            System.out.println("Wrong card number or PIN!");
        }
    }

    private static void closeAccount(AccountManager accountManager, Account account) {
        accountManager.deleteAccount(account);
        System.out.println("The account has been closed!");
    }

    private static void doTransfer(Scanner scanner, AccountManager accountManager, Account account) {
        System.out.println("Transfer");
        System.out.println("Enter card number:");
        String transferCardNumber = scanner.nextLine();
        if (transferCardNumber.isBlank()) {
            transferCardNumber = scanner.nextLine();
        }
        if (accountManager.isValidCardNumber(transferCardNumber)) {
            if (accountManager.getAccount(transferCardNumber) != null) {
                if(account.getCardNumber().equals(transferCardNumber)) {
                    System.out.println("You can't transfer money to the same account!");
                    return;
                }
                System.out.println("Enter how much money you want to transfer:");
                int transferAmount = scanner.nextInt();

                if (transferAmount <= account.getBalance()) {
                    account.setBalance(account.getBalance() - transferAmount);
                    Account transferAccount = accountManager.getAccount(transferCardNumber);
                    transferAccount.setBalance(transferAccount.getBalance() + transferAmount);
                    accountManager.updateAccount(account);
                    accountManager.updateAccount(transferAccount);
                    System.out.println("Success!");
                } else {
                    System.out.println("Not enough money!");
                }

            } else {
                System.out.println("Such a card does not exist.");
            }
        } else {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
        }
    }

    private static void addIncome(Scanner scanner, AccountManager accountManager, Account account) {
        System.out.println("Enter income:");
        int income = scanner.nextInt();
        account.setBalance(account.getBalance() + income);
        accountManager.updateAccount(account);
        System.out.println("Income was added!");
    }

    private static void printAccountMenu() {
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
    }

    private static void printMenu() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
    }
}