# Real-Time Fraud Detection Platform

Event-driven, explainable, production-grade fraud detection built with Spring Boot 4, Java 25, Kafka, Redis, and MySQL.

## Overview
This repository demonstrates a production-style real-time fraud detection architecture used by payment gateways, banks, and marketplaces. It focuses on low-latency decisioning, explainability, auditability, and human-in-the-loop review.

## Why this Project Exists
Fraud detection is a distributed decisioning system that must:
- operate in milliseconds
- detect behavioral anomalies over time
- explain every decision
- survive retries and failures
- support audits and investigations

## Core Design Principles
- Event-driven architecture
- Separation of concerns (signals, scoring, decision, action)
- Deterministic and explainable logic
- Idempotency everywhere
- Immutable audit trail
- Human review for gray areas
- Correlation ID across all services

## Services Overview

1. API Gateway & Auth  
    - Purpose: Single entry point, JWT validation, rate limiting, correlation ID generation  
    - Tech: Spring Cloud Gateway, OAuth2/JWT, WebFlux

2. Transaction Ingestion Service  
    - Purpose: Accept transaction intent, prevent duplicates, publish immutable facts  
    - Key concepts: Idempotency key, Outbox pattern  
    - Produces: `transactions.raw`

3. Enrichment Service  
    - Purpose: Convert raw transactions into fraud-relevant signals  
    - Signals: Velocity, geo anomalies, device reuse  
    - Algorithms: Sliding window counters (Redis + TTL), cardinality checks  
    - Consumes: `transactions.raw` → Produces: `transactions.enriched`

4. Risk Scoring Service  
    - Purpose: Quantify fraud risk without making decisions  
    - Algorithm (rule-based): risk_score = velocity_score + geo_score + device_score  
    - Consumes: `transactions.enriched` → Produces: `transactions.scored`

5. Decision Service  
    - Purpose: Apply business policy (not detection logic)  
    - Policy:
      - risk < 30 → ALLOW
      - 30 ≤ risk < 70 → REVIEW
      - risk ≥ 70 → BLOCK  
    - Consumes: `transactions.scored` → Produces: `transactions.decisioned`

6. Action Service  
    - Purpose: Safely execute decisions with side effects  
    - Decision → Action mapping:
      - ALLOW → Approve
      - REVIEW → Create case
      - BLOCK → Reject  
    - Consumes: `transactions.decisioned` → Produces: `transactions.actioned`

7. Audit & Compliance Ledger  
    - Purpose: Immutable audit trail for compliance and dispute resolution  
    - Consumes: `transactions.decisioned`, `transactions.actioned`  
    - Storage: Append-only MySQL tables

8. Alert & Case Management  
    - Purpose: Human-in-the-loop review for uncertain cases  
    - Triggered when: decision == REVIEW or BLOCK  
    - Storage: Case DB and analyst workflow states

## Testing the System
Entry API:
POST /api/transactions

Example ALLOW case:
```json
{
  "transactionId": "txn-allow-1",
  "customerId": "cust-1",
  "amount": 200,
  "currency": "USD",
  "country": "US",
  "deviceId": "dev-1"
}
```
Expected: decision = ALLOW, action = APPROVED

Example REVIEW case:
```json
{
  "transactionId": "txn-review-1",
  "customerId": "cust-review",
  "amount": 1200,
  "currency": "USD",
  "country": "IN",
  "deviceId": "dev-review"
}
```
Expected: decision = REVIEW, Case created

Example BLOCK case:
```json
{
  "transactionId": "txn-block-1",
  "customerId": "cust-risk",
  "amount": 5000,
  "currency": "USD",
  "country": "IN",
  "deviceId": "dev-shared"
}
```
Expected: decision = BLOCK, action = REJECTED

## Kafka Debug Commands
Watch raw events:
```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic transactions.raw
```
Watch decisions:
```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic transactions.decisioned
```

Kafka is the source of truth for debugging.

## Correlation ID (Critical Concept)
A Correlation ID ties together HTTP requests, Kafka events, logs, audit records, and analyst cases. Generate at the API gateway (or first entry service) and propagate via HTTP headers, Kafka headers, and the logging MDC. Without it, investigations and audits become impractical.

## Key Architectural Insights
- Fraud is detected by behavior over time, not single values  
- Scoring must be separate from decisions  
- Actions must be isolated from logic  
- Audit must be immutable  
- ML can assist, but rules must remain explainable

## Real-World Relevance
This architecture matches systems used in payment gateways, digital wallets, banks, marketplaces, and abuse-prevention platforms. Only scale and proprietary signals differ.
