package com.mailassistant.domain.model;

public record Chapter(long id, int number, String title) {
    @Override public String toString() { return title; }
}
