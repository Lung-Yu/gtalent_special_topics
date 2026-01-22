package com.example;

import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App {
    final Scanner scanner;

    public App() {
        scanner = new Scanner(System.in);
    }

    public void close() {
        scanner.close();
    }

    public static void main(String[] args) {
        App app = new App();

        app.start();

        app.close();
    }

    public void start() {

        int choose = 0;
        showMenu();
        do {
            choose = scanner.nextInt();
            switch (choose) {
                case 1:
                    System.out.println("1. 支出功能\n");
                    showMenu();
                    break;
                case 2:
                    System.out.println("2. 分類標籤管理\n");
                    showMenu();
                    break;
                case 3:
                    System.out.println("3. 消費紀錄查詢\n");
                    showMenu();
                    break;

                default:
                    System.out.print("請輸入0~3之間的數字 : ");
                    break;
            }

        } while (choose != 0);

        System.out.println();
        System.out.println("0. 退出系統");
    }

    private void showMenu() {
        System.out.println("=== 記帳系統選單 ===");
        System.out.println("1. 支出功能");
        System.out.println("2. 分類標籤管理");
        System.out.println("3. 消費紀錄查詢");
        System.out.println("0. 退出系統");
        System.out.print("請選擇功能 (0-3): ");
    }
}
