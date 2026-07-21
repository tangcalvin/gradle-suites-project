package com.philomath.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO with 10 fields of varied types for testing via Postman
 */
public class TenFieldDTO {
    private String title;
    private int count;
    private long id;
    private float weight;
    private double price;
    private boolean active;
    private Integer optionalNumber;
    private OffsetDateTime createdAt;
    private List<String> tags;
    private Address address;

    public TenFieldDTO() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Integer getOptionalNumber() { return optionalNumber; }
    public void setOptionalNumber(Integer optionalNumber) { this.optionalNumber = optionalNumber; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
}