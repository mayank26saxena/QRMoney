package com.indiahacks16.fintech.qrmoney;

public class Transaction {
    private String receiver, date_time;
    private String amount;
    public Transaction(String receiver, String date_time, String amount) {
        this.receiver = receiver;
        this.date_time = date_time;
        this.amount = amount;
    }
    String getReceiver() {
        return this.receiver;
    }
    String getDate_time() {
        return this.date_time;
    }
    String getAmount() {
        return this.amount;
    }
}
