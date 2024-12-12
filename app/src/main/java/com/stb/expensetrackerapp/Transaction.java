package com.stb.expensetrackerapp;

public class Transaction {
    public String transactionId;
    public String userId;
    public String balanceType;
    public String amount;
    public String operation;
    public String category;
    public String description;
    public String previousAmount;
    public String updatedAmount;
    public String currency;
    public String timestamp;
    public String status;

    public Transaction() {
    }
    public Transaction(String transactionId, String userId, String balanceType, String amount, String operation, String category, String description, String previousAmount, String updatedAmount, String currency, String timestamp, String status) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.balanceType = balanceType;
        this.amount = amount;
        this.operation = operation;
        this.category = category;
        this.description = description;
        this.previousAmount = previousAmount;
        this.updatedAmount = updatedAmount;
        this.currency = currency;
        this.timestamp = timestamp;
        this.status = status;
    }
    // getters and setters
    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getBalanceType() {
        return balanceType;
    }
    public void setBalanceType(String balanceType) {
        this.balanceType = balanceType;
    }
    public String getAmount() {
        return amount;
    }
    public void setAmount(String amount) {
        this.amount = amount;
    }
    public String getOperation() {
        return operation;
    }
    public void setOperation(String operation) {
        this.operation = operation;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getPreviousAmount() {
        return previousAmount;
    }
    public void setPreviousAmount(String previousAmount) {
        this.previousAmount = previousAmount;
    }
    public String getUpdatedAmount() {
        return updatedAmount;
    }
    public void setUpdatedAmount(String updatedAmount) {
        this.updatedAmount = updatedAmount;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    // toString method
}