package com.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;

public class test {
    public static void main(String[] args) {
        //String url = "jdbc:sqlserver://localhost:1433;databaseName=test;trustServerCertificate=true;integratedSecurity=true;";
        String url = "jdbc:sqlserver://localhost:1433;databaseName=test;trustServerCertificate=true;encrypt=false;trustServerCertificate=true;authenticationScheme=NTLM;integratedSecurity=true;useNTLMv2=true;username=IMED;password=1711;";
        try (Connection connection = DriverManager.getConnection(url)) {
            System.out.println("Connection successful!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
