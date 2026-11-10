# Spec Delta

## Purpose

Lets a client service or an orchestrator learn whether a running Math API instance is up and ready to accept Calculations, without exposing any other operational detail.

## ADDED Requirements

### Requirement: Health endpoint reports availability
The service SHALL answer `GET /actuator/health` with `200 OK` and a JSON body whose `status` is `UP` when the instance has started and can accept requests. The request needs no authentication and no input.

#### Scenario: Started instance is up
- **WHEN** a client sends `GET /actuator/health` to a started instance
- **THEN** the response is `200 OK` with content type `application/json` or `application/vnd.spring-boot.actuator.v3+json`
- **AND** the body's `status` field is `"UP"`

### Requirement: Only the health endpoint is exposed
The service SHALL NOT expose any operational endpoint over HTTP other than health and the `/actuator` index that links to it, so that configuration, environment and internal state are never readable by clients.

#### Scenario: Other operational endpoints are not served
- **WHEN** a client sends `GET /actuator/env`, `GET /actuator/beans` or `GET /actuator/info`
- **THEN** the response is `404 Not Found`

#### Scenario: Actuator index links only to health
- **WHEN** a client sends `GET /actuator`
- **THEN** the response is `200 OK`
- **AND** its `_links` name only `self` and `health` entries (including `health-path`)

#### Scenario: Health exposes no component details
- **WHEN** a client sends `GET /actuator/health`
- **THEN** the body has no `components` or `details` field
